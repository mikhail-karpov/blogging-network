package com.mikhailkarpov.users.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public abstract class AbstractIT {

    static final KeycloakContainer KEYCLOAK;

    static final GenericContainer REDIS;

    static {
        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("./bloggingnetwork-realm.json");

        REDIS = new GenericContainer("redis").withExposedPorts(6379);

        KEYCLOAK.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void configKeycloak(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> String.format("%s/realms/${app.keycloak.realm}", KEYCLOAK.getAuthServerUrl()));
    }

    @DynamicPropertySource
    static void configRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
        registry.add("spring.cache.type", () -> "redis");
    }
}
