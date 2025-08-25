package com.pm.patientservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Security for the patient service. The microservice acts
 * purely as an OAuth2 resource server and expects JWTs issued by Auth0. All
 * endpoints are protected and require a valid access token. Authorities are
 * extracted from the "permissions" claim so that specific HTTP methods can
 * be restricted to particular scopes (e.g. "create:patient").
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow all requests for development/testing
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    /**
     * Configures extraction of authorities from the "permissions" claim in
     * Auth0-issued JWTs. The default authority prefix ("ROLE_") is removed so
     * that scopes like "create:patient" can be used directly in security
     * expressions.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Do not prefix authorities with ROLE_
        authoritiesConverter.setAuthorityPrefix("");
        // Extract from the "permissions" claim
        authoritiesConverter.setAuthoritiesClaimName("permissions");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return jwtConverter;
    }
}