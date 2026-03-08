package backend.importer.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ConferenceListResponse(
        List<ConferenceRef> conferences) {
}
