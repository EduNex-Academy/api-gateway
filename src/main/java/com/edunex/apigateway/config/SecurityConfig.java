package com.edunex.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // We're using our custom corsFilter instead of default cors configuration
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Gateway specific endpoints
                        .pathMatchers("/api/gateway/**").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        // Actuator endpoints
                        .pathMatchers("/actuator/**").permitAll()
                        // Auth service endpoints (public)
                        .pathMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .pathMatchers("/api/auth/send-password-reset", "/api/auth/callback", "/api/auth/login-urls").permitAll()
                        .pathMatchers("/api/auth/diagnose", "/api/auth/health").permitAll()
                        // Subscription plans (public)
                        .pathMatchers("/api/v1/subscription-plans/**").permitAll()
                        // Webhooks (public but should be secured with webhook secrets)
                        .pathMatchers("/api/v1/webhooks/**").permitAll()
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                // Configure the gateway as a Resource Server to validate incoming JWTs.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Disable CSRF as we are using a stateless token-based approach.
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}
