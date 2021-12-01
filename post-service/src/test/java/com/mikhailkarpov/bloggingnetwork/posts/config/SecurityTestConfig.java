package com.mikhailkarpov.bloggingnetwork.posts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@TestConfiguration
public class SecurityTestConfig {

    public static final String TOKEN = "token";
    public static final String SUBJECT = UUID.randomUUID().toString();

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            Instant issuedAt = Instant.now();
            Instant expiresAt = Instant.now().plusSeconds(30);
            Map<String, Object> headers = Collections.singletonMap("alg", "none");
            Map<String, Object> claims = Collections.singletonMap("sub", SUBJECT);

            return new Jwt(TOKEN, issuedAt, expiresAt, headers, claims);
        };
    }
}
