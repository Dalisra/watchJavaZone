package backend.controller.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SpeakerResponse(
        String name,
        String bio) {
}
