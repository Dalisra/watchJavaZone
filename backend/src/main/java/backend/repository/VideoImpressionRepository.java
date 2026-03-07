package backend.repository;

import backend.domain.VideoImpression;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VideoImpressionRepository extends CrudRepository<VideoImpression, UUID> {
}
