package shared.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    @ConditionalOnProperty(value = "security.cors.enabled", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();

        if (props.getAllowedOrigins() != null && !props.getAllowedOrigins().isEmpty()) {
            config.setAllowedOrigins(props.getAllowedOrigins());
        }
        if (props.getAllowedOriginPatterns() != null && !props.getAllowedOriginPatterns().isEmpty()) {
            config.setAllowedOriginPatterns(props.getAllowedOriginPatterns());
        }
        config.setAllowedMethods(props.getAllowedMethods());
        config.setAllowedHeaders(props.getAllowedHeaders());
        config.setAllowCredentials(props.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
