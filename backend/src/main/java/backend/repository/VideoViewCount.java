package backend.repository;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record VideoViewCount(String videoId, Long viewCount) {
}
