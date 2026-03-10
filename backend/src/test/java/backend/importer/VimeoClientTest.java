package backend.importer;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@Property(name = "flyway.datasources.default.enabled", value = "false")
public class VimeoClientTest {

    @Inject
    VimeoClient vimeoClient;

    @Test
    void shouldGetOembedDataForVimeoVideo() {
        // Given a valid Vimeo video URL
        String videoUrl = "https://vimeo.com/1115459661";

        // When requesting oEmbed data
        var response = vimeoClient.getOembed(videoUrl);

        // Then the response should not be null and contain expected keys
        assertNotNull(response);
        assertEquals("video", response.type());
        assertNotNull(response.title());
    }
}
