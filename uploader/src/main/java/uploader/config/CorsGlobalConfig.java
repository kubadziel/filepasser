package uploader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Deprecated: CORS is now configured via shared.security.CorsConfig using properties.
 * This class is kept only to avoid bean-loading errors in existing contexts and
 * is disabled unless explicitly re-enabled.
 */
@Configuration
@ConditionalOnProperty(value = "security.cors.legacy-enabled", havingValue = "true")
@Deprecated
public class CorsGlobalConfig {
}
