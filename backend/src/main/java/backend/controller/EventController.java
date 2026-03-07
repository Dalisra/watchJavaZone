package backend.controller;

import backend.controller.dto.SearchEventRequest;
import backend.controller.dto.VideoEventRequest;
import backend.domain.SearchLog;
import backend.repository.SearchLogRepository;
import backend.repository.VideoEventRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.transaction.Transactional;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Map;

@Controller("/api/events")
@ExecuteOn(TaskExecutors.IO)
@Transactional
public class EventController {

    private final VideoEventRepository videoEventRepository;
    private final SearchLogRepository searchLogRepository;

    public EventController(VideoEventRepository videoEventRepository, SearchLogRepository searchLogRepository) {
        this.videoEventRepository = videoEventRepository;
        this.searchLogRepository = searchLogRepository;
    }

    /** Track a video playback event (play / pause / progress / ended / seek). */
    @Post("/video")
    @Status(HttpStatus.CREATED)
    public Map<String, String> trackVideoEvent(@Body VideoEventRequest req, HttpRequest<?> request) {
        String ip = extractIp(request);
        String ua = request.getHeaders().get("User-Agent");
        videoEventRepository.trackVideoEvent(
                req.videoId().toString(),
                req.userUuid().toString(),
                req.eventType(),
                req.playbackPositionSecs(),
                ip,
                ua);
        return Map.of("status", "ok");
    }

    /** Track a search query for analytics. */
    @Post("/search")
    @Status(HttpStatus.CREATED)
    public Map<String, String> trackSearch(@Body SearchEventRequest req, HttpRequest<?> request) {
        String ip = extractIp(request);
        String ua = request.getHeaders().get("User-Agent");
        searchLogRepository.save(new SearchLog(req.query(), req.resultsCount(), ip, ua, Instant.now()));
        return Map.of("status", "ok");
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
}
