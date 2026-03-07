package backend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VimeoUtilsTest {

    @Test
    void bareIdPassesThrough() {
        assertEquals("1115460917", VimeoUtils.extractId("1115460917"));
    }

    @Test
    void vimeoComUrl() {
        assertEquals("1115460917", VimeoUtils.extractId("https://vimeo.com/1115460917"));
    }

    @Test
    void playerVimeoUrl() {
        assertEquals("1115460917", VimeoUtils.extractId("https://player.vimeo.com/video/1115460917"));
    }

    @Test
    void urlWithQueryParams() {
        assertEquals("1115460917", VimeoUtils.extractId("https://player.vimeo.com/video/1115460917?h=abc123"));
    }

    @Test
    void urlWithTrailingSlash() {
        assertEquals("1115460917", VimeoUtils.extractId("https://vimeo.com/1115460917/"));
    }

    @Test
    void nullReturnsNull() {
        assertNull(VimeoUtils.extractId(null));
    }

    @Test
    void emptyStringReturnsNull() {
        assertNull(VimeoUtils.extractId(""));
    }

    @Test
    void blankStringReturnsNull() {
        assertNull(VimeoUtils.extractId("   "));
    }
}
