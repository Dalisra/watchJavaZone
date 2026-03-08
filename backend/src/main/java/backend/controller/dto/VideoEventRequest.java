package backend.controller.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public record VideoEventRequest(
        UUID videoId,
        UUID userUuid,
        String eventType, // play | pause | progress | ended | seek
        Integer playbackPositionSecs) {
}
