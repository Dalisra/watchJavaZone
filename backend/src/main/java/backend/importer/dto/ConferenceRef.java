package backend.importer.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ConferenceRef(
        String id,
        String name,
        String slug) {
}
