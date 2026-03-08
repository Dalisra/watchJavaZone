package backend.util;

import io.micronaut.http.HttpRequest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpRequestUtils {

    private HttpRequestUtils() {
        // static utility class
    }

    public static String extractIp(HttpRequest<?> request) {
        String forwarded = request.getHeaders().get("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        SocketAddress addr = request.getRemoteAddress();
        if (addr instanceof InetSocketAddress isa) {
            return isa.getAddress().getHostAddress();
        }
        return null;
    }

    public static String extractUserAgent(HttpRequest<?> request) {
        return request.getHeaders().get("User-Agent");
    }
}
