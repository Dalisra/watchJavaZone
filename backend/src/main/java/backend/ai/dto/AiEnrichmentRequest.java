package backend.ai.dto;

import java.util.List;

/** Data we send to Gemini for one video. */
public record AiEnrichmentRequest(
        String title,
        String abstractText,
        String intendedAudience,
        String suggestedKeywords,
        List<String> speakerBios) {
}
