package backend.controller;

import backend.controller.dto.SpeakerResponse;
import backend.controller.dto.VideoResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import jakarta.transaction.Transactional;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.TaskExecutors;
import backend.service.VideoTrackingService;
import java.util.stream.Collectors;

@Controller("/api/videos")
@ExecuteOn(TaskExecutors.IO)
@Transactional
public class VideoController {

    private static final String VIMEO_EMBED = "https://player.vimeo.com/video/";

    private final DataSource dataSource;
    private final VideoTrackingService trackingService;

    public VideoController(DataSource dataSource, VideoTrackingService trackingService) {
        this.dataSource = dataSource;
        this.trackingService = trackingService;
    }

    /** Paginated list of all videos, sorted by (base_score + view_boost) DESC. */
    @Get
    public List<VideoResponse> listVideos(
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size) {

        int limitedSize = Math.min(size, 20);

        String sql = """
                SELECT v.*, array_agg(sp.name ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_names,
                       array_agg(vs.bio  ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_bios
                FROM videos v
                LEFT JOIN video_speakers vs ON vs.video_id = v.id
                LEFT JOIN speakers sp ON vs.speaker_id = sp.id
                GROUP BY v.id
                ORDER BY (v.base_score + v.view_boost) DESC
                LIMIT ? OFFSET ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limitedSize);
            ps.setInt(2, page * limitedSize);
            List<VideoResponse> videos = mapVideos(ps.executeQuery(), false);
            triggerTracking(videos, "list");
            return videos;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list videos", e);
        }
    }

    /** Single video detail with related videos. */
    @Get("/{id}")
    public Optional<VideoResponse> getVideo(String id) {
        String sql = """
                SELECT v.*, array_agg(sp.name ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_names,
                       array_agg(vs.bio  ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_bios
                FROM videos v
                LEFT JOIN video_speakers vs ON vs.video_id = v.id
                LEFT JOIN speakers sp ON vs.speaker_id = sp.id
                WHERE v.id = ?
                GROUP BY v.id
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            List<VideoResponse> results = mapVideos(ps.executeQuery(), false);
            if (results.isEmpty())
                return Optional.empty();
            VideoResponse video = results.get(0);
            List<VideoResponse> related = findRelated(conn, video);
            triggerTracking(related, "related"); // track the related suggestions
            return Optional.of(withRelated(video, related));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get video", e);
        }
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
            @QueryValue(defaultValue = "") String lang) {
        String vectorCol = switch (lang) {
            case "en" -> "search_vector_en";
            case "no" -> "search_vector_no";
            default -> null;
        };
        String dictName = lang.equals("no") ? "norwegian" : "english";

        String whereClause = vectorCol != null
                ? "v." + vectorCol + " @@ plainto_tsquery('" + dictName + "', ?)"
                : "(v.search_vector_en @@ plainto_tsquery('english', ?) OR v.search_vector_no @@ plainto_tsquery('norwegian', ?))";
        String rankExpr = vectorCol != null
                ? "ts_rank(v." + vectorCol + ", plainto_tsquery('" + dictName + "', ?))"
                : "GREATEST(ts_rank(v.search_vector_en, plainto_tsquery('english', ?)), ts_rank(v.search_vector_no, plainto_tsquery('norwegian', ?)))";

        String sql = """
                SELECT v.*, array_agg(sp.name ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_names,
                       array_agg(vs.bio  ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_bios,
                       %s AS rank
                FROM videos v
                LEFT JOIN video_speakers vs ON vs.video_id = v.id
                LEFT JOIN speakers sp ON vs.speaker_id = sp.id
                WHERE %s
                GROUP BY v.id
                ORDER BY rank DESC, (v.base_score + v.view_boost) DESC
                LIMIT 20
                """.formatted(rankExpr, whereClause);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            // Bind params (rank expression first, then WHERE)
            if (vectorCol != null) {
                ps.setString(1, q); // rank
                ps.setString(2, q); // where
            } else {
                ps.setString(1, q); // rank en
                ps.setString(2, q); // rank no
                ps.setString(3, q); // where en
                ps.setString(4, q); // where no
            }
            List<VideoResponse> videos = mapVideos(ps.executeQuery(), false);
            triggerTracking(videos, "search");
            return videos;
        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    /** Videos with the most unique viewers in the last 7 days. */
    @Get("/trending")
    public List<VideoResponse> trending() {
        String sql = """
                SELECT v.*, array_agg(sp.name ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_names,
                       array_agg(vs.bio  ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_bios,
                       coalesce(s.viewers_last_7d, 0) AS trend_score
                FROM videos v
                LEFT JOIN video_view_stats s   ON s.video_id = v.id
                LEFT JOIN video_speakers vs    ON vs.video_id = v.id
                LEFT JOIN speakers sp          ON vs.speaker_id = sp.id
                GROUP BY v.id, s.viewers_last_7d
                ORDER BY trend_score DESC, (v.base_score + v.view_boost) DESC
                LIMIT 20
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            List<VideoResponse> videos = mapVideos(ps.executeQuery(), false);
            triggerTracking(videos, "trending");
            return videos;
        } catch (SQLException e) {
            throw new RuntimeException("Trending query failed", e);
        }
    }

    // ── Related videos ────────────────────────────────────────────────────────

    private List<VideoResponse> findRelated(Connection conn, VideoResponse video) throws SQLException {
        String sql = """
                SELECT v.*, array_agg(sp.name ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_names,
                       array_agg(vs.bio  ORDER BY sp.name) FILTER (WHERE sp.name IS NOT NULL) AS speaker_bios
                FROM videos v
                LEFT JOIN video_speakers vs ON vs.video_id = v.id
                LEFT JOIN speakers sp ON vs.speaker_id = sp.id
                WHERE v.id != ?
                  AND (
                    v.keywords && ?
                    OR EXISTS (
                        SELECT 1 FROM video_speakers vs2
                        JOIN speakers sp2 ON vs2.speaker_id = sp2.id
                        WHERE vs2.video_id = v.id
                        AND sp2.name = ANY(?)
                    )
                  )
                GROUP BY v.id
                ORDER BY (v.base_score + v.view_boost) DESC
                LIMIT 6
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[] kwArray = video.keywords() != null ? video.keywords().toArray(new String[0]) : new String[0];
            String[] spArray = video.speakers() != null
                    ? video.speakers().stream().map(SpeakerResponse::name).toArray(String[]::new)
                    : new String[0];
            ps.setString(1, video.id());
            ps.setArray(2, conn.createArrayOf("text", kwArray));
            ps.setArray(3, conn.createArrayOf("text", spArray));
            return mapVideos(ps.executeQuery(), false);
        }
    }

    // ── ResultSet mapping ─────────────────────────────────────────────────────

    private List<VideoResponse> mapVideos(ResultSet rs, boolean withRelated) throws SQLException {
        List<VideoResponse> out = new ArrayList<>();
        while (rs.next()) {
            out.add(mapRow(rs));
        }
        return out;
    }

    private VideoResponse mapRow(ResultSet rs) throws SQLException {
        String vimeoId = rs.getString("vimeo_id");
        List<String> keywords = arrayToList(rs, "keywords");
        List<String> aiKeywords = arrayToList(rs, "ai_keywords");

        // Build speakers list from aggregated arrays
        Array namesArr = rs.getArray("speaker_names");
        Array biosArr = rs.getArray("speaker_bios");
        List<SpeakerResponse> speakers = new ArrayList<>();
        if (namesArr != null) {
            String[] names = (String[]) namesArr.getArray();
            String[] bios = biosArr != null ? (String[]) biosArr.getArray() : new String[names.length];
            for (int i = 0; i < names.length; i++) {
                speakers.add(new SpeakerResponse(names[i], i < bios.length ? bios[i] : null));
            }
        }

        int base = rs.getInt("base_score");
        int boost = rs.getInt("view_boost");

        Timestamp start = rs.getTimestamp("start_time");
        Timestamp end = rs.getTimestamp("end_time");

        return new VideoResponse(
                rs.getString("id"),
                rs.getString("conference_id"),
                rs.getString("title"),
                rs.getString("title_en"),
                rs.getString("title_no"),
                rs.getString("abstract"),
                rs.getString("abstract_en"),
                rs.getString("abstract_no"),
                rs.getString("intended_audience"),
                vimeoId,
                vimeoId != null ? VIMEO_EMBED + vimeoId : null,
                rs.getString("language"),
                rs.getString("format"),
                rs.getObject("length_minutes", Integer.class),
                rs.getString("room"),
                start != null ? start.toInstant().toString() : null,
                end != null ? end.toInstant().toString() : null,
                keywords,
                aiKeywords,
                base,
                boost,
                base + boost,
                speakers,
                null // related populated separately in getVideo()
        );
    }

    private void triggerTracking(List<VideoResponse> videos, String endpoint) {
        if (videos == null || videos.isEmpty())
            return;
        List<String> ids = videos.stream().map(VideoResponse::id).collect(Collectors.toList());
        trackingService.trackImpressions(ids, endpoint);
    }

    private static List<String> arrayToList(ResultSet rs, String col) throws SQLException {
        Array arr = rs.getArray(col);
        if (arr == null)
            return List.of();
        String[] items = (String[]) arr.getArray();
        return items != null ? Arrays.asList(items) : List.of();
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
