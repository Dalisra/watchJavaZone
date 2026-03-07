package backend.service;

import backend.domain.VideoImpression;
import backend.repository.VideoImpressionRepository;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class VideoTrackingService {

    private static final Logger log = LoggerFactory.getLogger(VideoTrackingService.class);

    private final VideoImpressionRepository impressionRepository;

    public VideoTrackingService(VideoImpressionRepository impressionRepository) {
        this.impressionRepository = impressionRepository;
    }

    /**
     * Asynchronously logs that a set of videos was surfaced to a user.
     *
     * @param videoIds  The list of video IDs that were displayed.
     * @param endpoint  The identifier for where they were shown (e.g. "list", "search", "trending").
     * @param ipAddress The client IP address (may be null).
     * @param userAgent The client User-Agent header (may be null).
     */
    @ExecuteOn(TaskExecutors.VIRTUAL)
    public void trackImpressions(List<String> videoIds, String endpoint, String ipAddress, String userAgent) {
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }

        try {
            List<VideoImpression> impressions = videoIds.stream()
                    .map(id -> new VideoImpression(id, endpoint, ipAddress, userAgent))
                    .collect(Collectors.toList());

            impressionRepository.saveAll(impressions);
            log.debug("Tracked {} impressions for endpoint '{}'", videoIds.size(), endpoint);
        } catch (Exception e) {
            log.error("Failed to track video impressions", e);
        }
    }
}
