package backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** One language's translated content returned by Gemini. */
public record AiEnrichedContent(
        String title,
        @JsonProperty("abstract") String abstractText,
        String intendedAudience,
        List<String> aiKeywords) {
}
