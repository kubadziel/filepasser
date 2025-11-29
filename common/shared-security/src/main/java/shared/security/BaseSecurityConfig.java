package shared.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

public abstract class BaseSecurityConfig {

    protected SecurityFilterChain buildSecurityFilterChain(HttpSecurity http, String... permitAllPatterns) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    if (permitAllPatterns != null) {
                        for (String pattern : permitAllPatterns) {
                            auth.requestMatchers(pattern).permitAll();
                        }
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth.jwt());

        return http.build();
    }
}
