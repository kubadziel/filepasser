package shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * Comma-separated list of allowed origins. Defaults to localhost:3000 for dev.
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    /**
     * Optional list of allowed origin patterns (useful for wildcards).
     */
    private List<String> allowedOriginPatterns = List.of();

    /**
     * Allowed HTTP methods, default is all.
     */
    private List<String> allowedMethods = List.of("*");

    /**
     * Allowed headers, default is all.
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Whether credentials (cookies/authorization headers) are allowed.
     */
    private boolean allowCredentials = true;

    /**
     * Toggle CORS configuration.
     */
    private boolean enabled = true;
}
