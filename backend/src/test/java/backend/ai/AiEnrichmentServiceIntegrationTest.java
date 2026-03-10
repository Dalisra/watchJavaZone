package backend.ai;

import backend.ai.dto.AiEnrichmentRequest;
import backend.ai.dto.AiEnrichmentResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = ".+")
@Property(name = "gemini.model", value = "gemini-2.5-flash")
class AiEnrichmentServiceIntegrationTest {

    @Inject
    AiEnrichmentService aiEnrichmentService;

    @Test
    void realGeminiCallTranslatesNorwegianTalk() {
        AiEnrichmentRequest request = new AiEnrichmentRequest(
                "Maskinlæring i Java",
                "En praktisk introduksjon til hvordan du integrerer maskinlæringsmodeller i Java-applikasjoner ved hjelp av moderne rammeverk.",
                "Java-utviklere med grunnleggende interesse for maskinlæring",
                "machine-learning,java,ai,models",
                List.of("Senior Java-utvikler med 10 års erfaring og bakgrunn innen maskinlæring")
        );

        Optional<AiEnrichmentResponse> result = aiEnrichmentService.enrich(request);

        assertTrue(result.isPresent(), "Expected a response from Gemini");
        AiEnrichmentResponse response = result.get();

        assertNotNull(response.english(), "English content should be present");
        assertNotNull(response.english().title(), "English title should be present");
        assertFalse(response.english().title().isBlank(), "English title should not be blank");
        assertNotNull(response.english().abstractText(), "English abstract should be present");
        assertNotNull(response.english().aiKeywords(), "English keywords should be present");
        assertFalse(response.english().aiKeywords().isEmpty(), "English keywords should not be empty");

        assertNotNull(response.norwegian(), "Norwegian content should be present");
        assertNotNull(response.norwegian().title(), "Norwegian title should be present");
        assertFalse(response.norwegian().title().isBlank(), "Norwegian title should not be blank");
        assertNotNull(response.norwegian().abstractText(), "Norwegian abstract should be present");
        assertNotNull(response.norwegian().aiKeywords(), "Norwegian keywords should be present");
        assertFalse(response.norwegian().aiKeywords().isEmpty(), "Norwegian keywords should not be empty");

        System.out.println("=== English ===");
        System.out.println("Title    : " + response.english().title());
        System.out.println("Abstract : " + response.english().abstractText());
        System.out.println("Audience : " + response.english().intendedAudience());
        System.out.println("Keywords : " + response.english().aiKeywords());
        System.out.println("=== Norwegian ===");
        System.out.println("Title    : " + response.norwegian().title());
        System.out.println("Abstract : " + response.norwegian().abstractText());
        System.out.println("Audience : " + response.norwegian().intendedAudience());
        System.out.println("Keywords : " + response.norwegian().aiKeywords());
    }
}
