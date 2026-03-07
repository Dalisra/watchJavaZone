package backend.controller;

import backend.controller.dto.SearchEventRequest;
import backend.controller.dto.VideoEventRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;

import jakarta.transaction.Transactional;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.TaskExecutors;

@Controller("/api/events")
@ExecuteOn(TaskExecutors.IO)
@Transactional
public class EventController {

    private final DataSource dataSource;

    public EventController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Track a video playback event (play / pause / progress / ended / seek). */
    @Post("/video")
    @Status(HttpStatus.CREATED)
    public Map<String, String> trackVideoEvent(@Body VideoEventRequest req) {
        String sql = """
                INSERT INTO video_events (video_id, user_uuid, event_type, playback_position_secs)
                VALUES (?, ?, ?::video_event_type, ?)
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.videoId().toString());
            ps.setString(2, req.userUuid().toString());
            ps.setString(3, req.eventType());
            if (req.playbackPositionSecs() != null) {
                ps.setInt(4, req.playbackPositionSecs());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.executeUpdate();
            return Map.of("status", "ok");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record video event", e);
        }
    }

    /** Track a search query for analytics. */
    @Post("/search")
    @Status(HttpStatus.CREATED)
    public Map<String, String> trackSearch(@Body SearchEventRequest req) {
        String sql = """
                INSERT INTO search_logs (query, results_count)
                VALUES (?, ?)
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.query());
            if (req.resultsCount() != null) {
                ps.setInt(2, req.resultsCount());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.executeUpdate();
            return Map.of("status", "ok");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record search", e);
        }
    }
}
