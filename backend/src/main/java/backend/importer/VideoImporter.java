package backend.importer;

import backend.ai.AiEnrichmentService;
import backend.ai.dto.AiEnrichmentRequest;
import backend.ai.dto.AiEnrichmentResponse;
import backend.domain.Conference;
import backend.domain.Speaker;
import backend.domain.Video;
import backend.domain.VideoSpeaker;
import backend.importer.dto.ConferenceRef;
import backend.importer.dto.SleepingPillSession;
import backend.importer.dto.SleepingPillSpeaker;
import backend.importer.dto.VimeoOembedResponse;
import backend.repository.ConferenceRepository;
import backend.repository.SpeakerRepository;
import backend.repository.VideoRepository;
import backend.repository.VideoSpeakerRepository;
import backend.util.VimeoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class VideoImporter {

    private static final Logger log = LoggerFactory.getLogger(VideoImporter.class);

    private final SleepingPillClient sleepingPillClient;
    private final VimeoClient vimeoClient;
    private final ObjectMapper objectMapper;
    private final AiEnrichmentService aiEnrichmentService;
    private final ConferenceRepository conferenceRepository;
    private final SpeakerRepository speakerRepository;
    private final VideoRepository videoRepository;
    private final VideoSpeakerRepository videoSpeakerRepository;

    public VideoImporter(SleepingPillClient sleepingPillClient,
                         VimeoClient vimeoClient,
                         ObjectMapper objectMapper,
                         AiEnrichmentService aiEnrichmentService,
                         ConferenceRepository conferenceRepository,
                         SpeakerRepository speakerRepository,
                         VideoRepository videoRepository,
                         VideoSpeakerRepository videoSpeakerRepository) {
        this.sleepingPillClient = sleepingPillClient;
        this.vimeoClient = vimeoClient;
        this.objectMapper = objectMapper;
        this.aiEnrichmentService = aiEnrichmentService;
        this.conferenceRepository = conferenceRepository;
        this.speakerRepository = speakerRepository;
        this.videoRepository = videoRepository;
        this.videoSpeakerRepository = videoSpeakerRepository;
    }

    /**
     * Full import run. Should be triggered only via GET /api/import.
     *
     * @param force When true, re-imports all conferences regardless of completed flag,
     *              re-runs AI enrichment, and re-fetches Vimeo oEmbed data.
     * @return summary map with counts per conference
     */
    public Map<String, Integer> importAll(boolean force, Integer importYearOnly) {
        log.info("VideoImporter: starting import (force={})", force);
        Map<String, Integer> summary = new LinkedHashMap<>();

        List<ConferenceRef> conferences = sleepingPillClient.getAllConferences().conferences();
        List<ConferenceRef> sorted = conferences.stream()
                .sorted(Comparator.comparingInt(c -> extractYear(c.name())))
                .toList();
        int total = sorted.size();

        for (int i = 0; i < total; i++) {
            ConferenceRef confRef = sorted.get(i);
            int rank = total - i;
            int baseScore = Math.max(0, 150 - (rank - 1) * 10);
            int year = extractYear(confRef.name());

            if(importYearOnly != null && !Objects.equals(importYearOnly, year)) continue;

            Optional<Conference> optConf = conferenceRepository.findById(confRef.id());
            if (!force && optConf.isPresent() && Boolean.TRUE.equals(optConf.get().getCompleted())) {
                log.info("VideoImporter: skipping {}, already fully imported", confRef.slug());
                summary.put(confRef.slug(), 0);
                continue;
            }

            try {
                int count = importConference(confRef, year, baseScore, force);
                summary.put(confRef.slug(), count);
            } catch (Exception e) {
                log.error("Failed to import conference {}: {}", confRef.slug(), e.getMessage());
            }
        }

        log.info("VideoImporter: import complete. Summary: {}", summary);
        return summary;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected int importConference(ConferenceRef confRef, int year, int baseScore, boolean force) {
        upsertConference(confRef.id(), confRef.slug(), confRef.name(), year);
        List<SleepingPillSession> sessions;
        try {
            sessions = sleepingPillClient.getSessions(confRef.slug()).sessions();
        } catch (Exception e) {
            log.warn("Failed to fetch sessions for {}: {}", confRef.slug(), e.getMessage());
            throw new RuntimeException("Failed to fetch sessions", e);
        }

        int count = 0;
        for (SleepingPillSession session : sessions) {
            String vimeoId = VimeoUtils.extractId(session.video());
            if (vimeoId == null || vimeoId.isBlank())
                continue;

            try {
                importVideo(session, vimeoId, confRef.id(), baseScore, force);
                count++;
            } catch (Exception e) {
                log.error("Failed to import video {}: {}", session.id(), e.getMessage());
            }
        }

        videoRepository.rebuildSearchVectorEn(confRef.id());
        videoRepository.rebuildSearchVectorNo(confRef.id());
        videoRepository.updateViewBoosts();
        markConferenceCompleted(confRef.id());

        log.info("VideoImporter: {} → {} videos imported (force={})", confRef.slug(), count, force);
        return count;
    }

    private void importVideo(SleepingPillSession session, String vimeoId, String confId, int baseScore, boolean force) {
        String videoId = session.id();
        List<String> keywords = parseKeywords(session.suggestedKeywords());

        Optional<Video> existingOpt = videoRepository.findById(videoId);
        boolean alreadyEnriched = existingOpt.map(v -> v.getTitleEn() != null).orElse(false);

        // --- AI enrichment ---
        Optional<AiEnrichmentResponse> enriched = Optional.empty();
        if (!alreadyEnriched || force) {
            AiEnrichmentRequest req = new AiEnrichmentRequest(
                    session.title(), session.abstractText(), session.intendedAudience(),
                    session.suggestedKeywords(), speakerBios(session));
            enriched = aiEnrichmentService.enrich(req);
        }

        // --- Vimeo oEmbed ---
        boolean alreadyHasVimeoData = existingOpt.map(v -> v.getThumbnailUrl() != null).orElse(false);
        String vimeoRawJson = null;
        VimeoOembedResponse vimeoData = null;
        if (!alreadyHasVimeoData || force) {
            try {
                vimeoRawJson = vimeoClient.getOembed("https://vimeo.com/" + vimeoId);
                vimeoData = objectMapper.readValue(vimeoRawJson, VimeoOembedResponse.class);
            } catch (Exception e) {
                log.warn("Failed to fetch Vimeo oEmbed for vimeoId={}: {}", vimeoId, e.getMessage());
            }
        }

        // --- Build entity ---
        Video video = existingOpt.orElseGet(Video::new);
        video.setId(videoId);
        video.setConferenceId(confId);
        video.setTitle(session.title());
        video.setAbstractText(session.abstractText());
        video.setIntendedAudience(session.intendedAudience());
        video.setVimeoId(vimeoId);
        video.setLanguage(session.language());
        video.setFormat(session.format());
        video.setLengthMinutes(parseLength(session.length()));
        video.setRoom(session.room());
        video.setStartTime(parseInstant(session.startTimeZulu()));
        video.setEndTime(parseInstant(session.endTimeZulu()));
        video.setKeywords(keywords);
        video.setBaseScore(baseScore);
        video.setUpdatedAt(Instant.now());

        if (existingOpt.isEmpty()) {
            video.setCreatedAt(Instant.now());
            video.setViewBoost(0);
        }

        if (enriched.isPresent()) {
            AiEnrichmentResponse res = enriched.get();
            if (res.english() != null && res.english().title() != null) {
                video.setTitleEn(res.english().title());
                video.setAbstractEn(res.english().abstractText());
                video.setIntendedAudienceEn(res.english().intendedAudience());
                video.setAiKeywords(res.english().aiKeywords());
            }
            if (res.norwegian() != null && res.norwegian().title() != null) {
                video.setTitleNo(res.norwegian().title());
                video.setAbstractNo(res.norwegian().abstractText());
                video.setIntendedAudienceNo(res.norwegian().intendedAudience());
                if (video.getAiKeywords() == null && res.norwegian().aiKeywords() != null) {
                    video.setAiKeywords(res.norwegian().aiKeywords());
                } else if (video.getAiKeywords() != null && res.norwegian().aiKeywords() != null) {
                    Set<String> merged = new LinkedHashSet<>(video.getAiKeywords());
                    merged.addAll(res.norwegian().aiKeywords());
                    video.setAiKeywords(new ArrayList<>(merged));
                }
            }
        }

        if (vimeoData != null) {
            video.setThumbnailUrl(vimeoData.thumbnailUrl());
            video.setVimeoDuration(vimeoData.duration());
        }

        if (existingOpt.isPresent()) {
            videoRepository.update(video);
        } else {
            videoRepository.save(video);
        }

        if (vimeoRawJson != null) {
            videoRepository.updateVimeoOembed(videoId, vimeoRawJson);
        }

        List<SleepingPillSpeaker> speakers = session.speakers() != null ? session.speakers() : List.of();
        for (SleepingPillSpeaker speaker : speakers) {
            if (speaker.name() == null || speaker.name().isBlank())
                continue;
            String speakerId = UUID.nameUUIDFromBytes(speaker.name().toLowerCase().getBytes()).toString();
            upsertSpeaker(speakerId, speaker.name());
            upsertVideoSpeaker(videoId, speakerId, speaker.bio());
        }
    }

    private void upsertConference(String id, String slug, String name, int year) {
        Optional<Conference> opt = conferenceRepository.findById(id);
        if (opt.isPresent()) {
            Conference c = opt.get();
            c.setName(name);
            c.setYear(year);
            conferenceRepository.update(c);
        } else {
            Conference c = new Conference(id, slug, name, year);
            c.setCompleted(false);
            conferenceRepository.save(c);
        }
    }

    private void markConferenceCompleted(String id) {
        conferenceRepository.findById(id).ifPresent(c -> {
            c.setCompleted(true);
            conferenceRepository.update(c);
        });
    }

    private void upsertSpeaker(String id, String name) {
        if (speakerRepository.findById(id).isEmpty()) {
            speakerRepository.save(new Speaker(id, name));
        }
    }

    private void upsertVideoSpeaker(String videoId, String speakerId, String bio) {
        Optional<VideoSpeaker> opt = videoSpeakerRepository.findByVideoIdAndSpeakerId(videoId, speakerId);
        if (opt.isPresent()) {
            VideoSpeaker vs = opt.get();
            vs.setBio(bio);
            videoSpeakerRepository.update(vs);
        } else {
            videoSpeakerRepository.save(new VideoSpeaker(videoId, speakerId, bio));
        }
    }

    private static int extractYear(String name) {
        try {
            return Integer.parseInt(name.replaceAll("\\D", "").substring(0, 4));
        } catch (Exception e) {
            return 0;
        }
    }

    private static List<String> parseKeywords(String csv) {
        if (csv == null || csv.isBlank())
            return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static Integer parseLength(String len) {
        try {
            return Integer.parseInt(len);
        } catch (Exception e) {
            return null;
        }
    }

    private static Instant parseInstant(String iso) {
        try {
            return Instant.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> speakerBios(SleepingPillSession s) {
        if (s.speakers() == null)
            return List.of();
        return s.speakers().stream()
                .map(sp -> sp.bio() != null ? sp.bio() : "")
                .filter(b -> !b.isBlank())
                .collect(Collectors.toList());
    }
}
