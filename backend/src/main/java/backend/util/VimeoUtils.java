package backend.util;

public class VimeoUtils {

    private VimeoUtils() {
    }

    /**
     * Extracts a bare Vimeo video ID from any of the following formats:
     * <ul>
     * <li>{@code "1115460917"}</li>
     * <li>{@code "https://vimeo.com/1115460917"}</li>
     * <li>{@code "https://player.vimeo.com/video/1115460917"}</li>
     * <li>{@code "https://player.vimeo.com/video/1115460917?h=abc&..."}</li>
     * </ul>
     *
     * @return the bare numeric ID, or {@code null} if the input is blank/invalid
     */
    public static String extractId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        // Strip query string
        int q = s.indexOf('?');
        if (q >= 0)
            s = s.substring(0, q);
        // Strip trailing slashes
        while (s.endsWith("/"))
            s = s.substring(0, s.length() - 1);
        // Take the last path segment
        int slash = s.lastIndexOf('/');
        if (slash >= 0)
            s = s.substring(slash + 1);
        // Keep only leading digits
        s = s.replaceAll("[^0-9].*", "");
        return s.isBlank() ? null : s;
    }
}
