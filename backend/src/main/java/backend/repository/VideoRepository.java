package backend.repository;

import backend.domain.Video;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.annotation.Query;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VideoRepository extends CrudRepository<Video, String> {

    @Query("UPDATE videos SET search_vector_en = (" +
            "  setweight(to_tsvector('english', coalesce(title_en, title, '')), 'A') || " +
            "  setweight(to_tsvector('simple', coalesce(array_to_string(keywords, ' '), '') || ' ' || coalesce(array_to_string(ai_keywords, ' '), '')), 'B') || "
            +
            "  setweight(to_tsvector('simple', coalesce((SELECT string_agg(sp.name, ' ') FROM video_speakers vs JOIN speakers sp ON vs.speaker_id = sp.id WHERE vs.video_id = videos.id), '')), 'B') || "
            +
            "  setweight(to_tsvector('english', coalesce(abstract_en, abstract, '') || ' ' || coalesce(intended_audience_en, intended_audience, '')), 'C')"
            +
            ") WHERE conference_id = :conferenceId")
    void rebuildSearchVectorEn(String conferenceId);

    @Query("UPDATE videos SET search_vector_no = (" +
            "  setweight(to_tsvector('norwegian', coalesce(title_no, title, '')), 'A') || " +
            "  setweight(to_tsvector('simple', coalesce(array_to_string(keywords, ' '), '') || ' ' || coalesce(array_to_string(ai_keywords, ' '), '')), 'B') || "
            +
            "  setweight(to_tsvector('simple', coalesce((SELECT string_agg(sp.name, ' ') FROM video_speakers vs JOIN speakers sp ON vs.speaker_id = sp.id WHERE vs.video_id = videos.id), '')), 'B') || "
            +
            "  setweight(to_tsvector('norwegian', coalesce(abstract_no, abstract, '') || ' ' || coalesce(intended_audience_no, intended_audience, '')), 'C')"
            +
            ") WHERE conference_id = :conferenceId")
    void rebuildSearchVectorNo(String conferenceId);

    @Query("UPDATE videos SET view_boost = COALESCE((SELECT LEAST(viewers_last_7d * 2, 100) FROM video_view_stats WHERE video_id = videos.id), 0)")
    void updateViewBoosts();
}
