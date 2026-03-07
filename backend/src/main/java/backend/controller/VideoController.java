package backend.controller;

import backend.controller.dto.SpeakerResponse;
import backend.controller.dto.VideoResponse;
import backend.domain.Video;
import backend.repository.SpeakerRow;
import backend.repository.VideoRepository;
import backend.repository.VideoSpeakerRepository;
import backend.service.VideoTrackingService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.transaction.Transactional;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.stream.Collectors;

@Controller("/api/videos")
@ExecuteOn(TaskExecutors.VIRTUAL)
@Transactional
public class VideoController {

    private static final String VIMEO_EMBED = "https://player.vimeo.com/video/";

    private final VideoRepository videoRepository;
    private final VideoSpeakerRepository videoSpeakerRepository;
    private final VideoTrackingService trackingService;

    public VideoController(VideoRepository videoRepository,
                           VideoSpeakerRepository videoSpeakerRepository,
                           VideoTrackingService trackingService) {
        this.videoRepository = videoRepository;
        this.videoSpeakerRepository = videoSpeakerRepository;
        this.trackingService = trackingService;
    }

    /** Paginated list of all videos, sorted by (base_score + view_boost) DESC. */
    @Get
    public List<VideoResponse> listVideos(
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size,
            @QueryValue(defaultValue = "") String lang,
            HttpRequest<?> request) {

        int limitedSize = Math.min(size, 20);
        List<Video> videos = videoRepository.listVideos(limitedSize, page * limitedSize);
        List<VideoResponse> responses = toResponses(videos, null, lang);
        trackImpressions(responses, "list", request);
        return responses;
    }

    /** Single video detail with related videos. */
    @Get("/{id}")
    public Optional<VideoResponse> getVideo(
            String id,
            @QueryValue(defaultValue = "") String lang,
            HttpRequest<?> request) {
        Optional<Video> found = videoRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Video video = found.get();
        List<Video> related = videoRepository.findRelated(id);

        Map<String, List<SpeakerRow>> speakerMap = loadSpeakers(
                collectIds(List.of(video), related));

        VideoResponse videoResponse = toResponse(video, speakerMap.get(video.getId()), null, lang);
        List<VideoResponse> relatedResponses = toResponses(related, speakerMap, lang);

        String ip = extractIp(request);
        String ua = request.getHeaders().get("User-Agent");
        asyncTrack(List.of(video.getId()), "detail", ip, ua);
        asyncTrack(relatedResponses.stream().map(VideoResponse::id).collect(Collectors.toList()),
                "related", ip, ua);

        return Optional.of(withRelated(videoResponse, relatedResponses));
    }

    /**
     * Full-text search.
     *
     * @param q    search query
     * @param lang "en" or "no" — defaults to searching both vectors
     */
    @Get("/search")
    public List<VideoResponse> search(
            @QueryValue String q,
            @QueryValue(defaultValue = "") String lang,
            HttpRequest<?> request) {

        List<Video> videos = switch (lang) {
            case "en" -> videoRepository.searchEn(q);
            case "no" -> videoRepository.searchNo(q);
            default -> videoRepository.searchBoth(q);
        };
        List<VideoResponse> responses = toResponses(videos, null, lang);
        trackImpressions(responses, "search", request);
        return responses;
    }

    /** Videos with the most unique viewers in the last 7 days. */
    @Get("/trending")
    public List<VideoResponse> trending(
            @QueryValue(defaultValue = "") String lang,
            HttpRequest<?> request) {
        List<Video> videos = videoRepository.findTrending();
        List<VideoResponse> responses = toResponses(videos, null, lang);
        trackImpressions(responses, "trending", request);
        return responses;
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private List<VideoResponse> toResponses(List<Video> videos, Map<String, List<SpeakerRow>> speakerMap, String lang) {
        if (videos.isEmpty()) {
            return List.of();
        }
        Map<String, List<SpeakerRow>> speakers = speakerMap != null
                ? speakerMap
                : loadSpeakers(videos.stream().map(Video::getId).collect(Collectors.toList()));
        return videos.stream()
                .map(v -> toResponse(v, speakers.get(v.getId()), null, lang))
                .collect(Collectors.toList());
    }

    private VideoResponse toResponse(Video v, List<SpeakerRow> speakers, List<VideoResponse> related, String lang) {
        List<SpeakerResponse> speakerResponses = speakers == null ? List.of() :
                speakers.stream()
                        .map(s -> new SpeakerResponse(s.name(), s.bio()))
                        .collect(Collectors.toList());

        int base = v.getBaseScore() != null ? v.getBaseScore() : 0;
        int boost = v.getViewBoost() != null ? v.getViewBoost() : 0;

        String title = switch (lang) {
            case "en" -> v.getTitleEn() != null ? v.getTitleEn() : v.getTitle();
            case "no" -> v.getTitleNo() != null ? v.getTitleNo() : v.getTitle();
            default -> v.getTitle();
        };
        String abstractText = switch (lang) {
            case "en" -> v.getAbstractEn() != null ? v.getAbstractEn() : v.getAbstractText();
            case "no" -> v.getAbstractNo() != null ? v.getAbstractNo() : v.getAbstractText();
            default -> v.getAbstractText();
        };

        return new VideoResponse(
                v.getId(),
                v.getConferenceId(),
                title,
                v.getTitleEn(),
                v.getTitleNo(),
                abstractText,
                v.getAbstractEn(),
                v.getAbstractNo(),
                v.getIntendedAudience(),
                v.getVimeoId(),
                v.getVimeoId() != null ? VIMEO_EMBED + v.getVimeoId() : null,
                v.getLanguage(),
                v.getFormat(),
                v.getLengthMinutes(),
                v.getRoom(),
                v.getStartTime() != null ? v.getStartTime().toString() : null,
                v.getEndTime() != null ? v.getEndTime().toString() : null,
                v.getKeywords() != null ? v.getKeywords() : List.of(),
                v.getAiKeywords() != null ? v.getAiKeywords() : List.of(),
                base,
                boost,
                base + boost,
                speakerResponses,
                related);
    }

    private Map<String, List<SpeakerRow>> loadSpeakers(List<String> videoIds) {
        if (videoIds.isEmpty()) {
            return Map.of();
        }
        return videoSpeakerRepository.findSpeakersForVideoIds(videoIds)
                .stream()
                .collect(Collectors.groupingBy(SpeakerRow::videoId));
    }

    private List<String> collectIds(List<Video> primary, List<Video> secondary) {
        List<String> ids = new ArrayList<>();
        primary.forEach(v -> ids.add(v.getId()));
        secondary.forEach(v -> ids.add(v.getId()));
        return ids;
    }

    private void trackImpressions(List<VideoResponse> responses, String endpoint, HttpRequest<?> request) {
        if (responses.isEmpty()) {
            return;
        }
        asyncTrack(responses.stream().map(VideoResponse::id).collect(Collectors.toList()),
                endpoint, extractIp(request), request.getHeaders().get("User-Agent"));
    }

    private void asyncTrack(List<String> ids, String endpoint, String ip, String ua) {
        if (ids.isEmpty()) {
            return;
        }
        Thread.ofVirtual().start(() -> trackingService.trackImpressions(ids, endpoint, ip, ua));
    }

    private String extractIp(HttpRequest<?> request) {
        String forwarded = request.getHeaders().get("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        SocketAddress addr = request.getRemoteAddress();
        if (addr instanceof InetSocketAddress isa) {
            return isa.getAddress().getHostAddress();
        }
        return null;
    }

    private static VideoResponse withRelated(VideoResponse v, List<VideoResponse> related) {
        return new VideoResponse(
                v.id(), v.conferenceId(), v.title(), v.titleEn(), v.titleNo(),
                v.abstractText(), v.abstractEn(), v.abstractNo(),
                v.intendedAudience(), v.vimeoId(), v.embedUrl(),
                v.language(), v.format(), v.lengthMinutes(), v.room(),
                v.startTime(), v.endTime(),
                v.keywords(), v.aiKeywords(),
                v.baseScore(), v.viewBoost(), v.totalScore(),
                v.speakers(), related);
    }
}
