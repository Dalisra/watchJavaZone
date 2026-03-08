package backend.controller;

import backend.controller.dto.VideoEventRequest;
import backend.repository.VideoEventRepository;
import backend.util.HttpRequestUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.transaction.Transactional;

import java.util.Map;

@Controller("/api/events")
@ExecuteOn(TaskExecutors.BLOCKING)
@Transactional
public class EventController {

    private final VideoEventRepository videoEventRepository;

    public EventController(VideoEventRepository videoEventRepository) {
        this.videoEventRepository = videoEventRepository;
    }

    /** Track a video playback event (play / pause / progress / ended / seek). */
    @Post("/video")
    @Status(HttpStatus.CREATED)
    public Map<String, String> trackVideoEvent(@Body VideoEventRequest req, HttpRequest<?> request) {
        String ip = HttpRequestUtils.extractIp(request);
        String ua = HttpRequestUtils.extractUserAgent(request);
        videoEventRepository.trackVideoEvent(
                req.videoId().toString(),
                req.userUuid().toString(),
                req.eventType(),
                req.playbackPositionSecs(),
                ip,
                ua);
        return Map.of("status", "ok");
    }
}
