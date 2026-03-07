package backend.repository;

import backend.domain.VideoSpeaker;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VideoSpeakerRepository extends CrudRepository<VideoSpeaker, String> {
    Optional<VideoSpeaker> findByVideoIdAndSpeakerId(String videoId, String speakerId);
}
