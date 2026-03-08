package backend.repository;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record SpeakerRow(String videoId, String name, String bio) {
}
