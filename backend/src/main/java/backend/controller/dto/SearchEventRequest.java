package backend.controller.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SearchEventRequest(
        String query,
        Integer resultsCount) {
}
