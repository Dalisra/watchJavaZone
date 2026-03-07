package backend.repository;

import backend.domain.VideoSpeaker;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VideoSpeakerRepository extends CrudRepository<VideoSpeaker, String> {
    Optional<VideoSpeaker> findByVideoIdAndSpeakerId(String videoId, String speakerId);

    @Query("SELECT vs.video_id, sp.name, vs.bio " +
           "FROM video_speakers vs JOIN speakers sp ON vs.speaker_id = sp.id " +
           "WHERE vs.video_id IN (:videoIds) ORDER BY vs.video_id, sp.name")
    List<SpeakerRow> findSpeakersForVideoIds(List<String> videoIds);
}
