package backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record SleepingPillSession(
        String id,
        String conferenceId,
        String title,
        @JsonProperty("abstract") String abstractText,
        String intendedAudience,
        String suggestedKeywords,
        String language,
        String format,
        String length,
        String room,
        String video,
        String startTimeZulu,
        String endTimeZulu,
        List<SleepingPillSpeaker> speakers) {
}
