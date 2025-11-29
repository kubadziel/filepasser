package uploader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import shared.security.BaseSecurityConfig;
import shared.security.DisabledSecurityConfig;

@Configuration
@EnableMethodSecurity
@Import(DisabledSecurityConfig.class)
@ConditionalOnProperty(value = "security.enabled", matchIfMissing = true)
public class SecurityConfig extends BaseSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return buildSecurityFilterChain(http, "/actuator/health");
    }
}
