package backend.importer.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SleepingPillSpeaker(
        String name,
        String bio) {
}
