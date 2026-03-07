package backend.ai.dto;

/** Full AI enrichment response containing both language versions. */
public record AiEnrichmentResponse(
        AiEnrichedContent english,
        AiEnrichedContent norwegian) {
}
