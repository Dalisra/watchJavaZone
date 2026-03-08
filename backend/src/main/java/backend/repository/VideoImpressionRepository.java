package backend.repository;

import backend.domain.VideoImpression;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VideoImpressionRepository extends CrudRepository<VideoImpression, UUID> {

    @Query("SELECT video_id, COUNT(*) AS view_count " +
            "FROM video_impressions " +
            "WHERE endpoint = 'detail' AND video_id IN (:videoIds) " +
            "GROUP BY video_id")
    List<VideoViewCount> countDetailViewsForVideos(List<String> videoIds);

    boolean existsByVideoIdAndEndpointAndIpAddressAndUserAgentAndCreatedAtGreaterThan(
            String videoId, String endpoint, String ipAddress, String userAgent, java.time.Instant since);
}
