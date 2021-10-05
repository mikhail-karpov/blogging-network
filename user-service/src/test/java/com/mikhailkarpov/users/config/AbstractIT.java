package com.mikhailkarpov.users.config;

import com.mikhailkarpov.users.repository.FollowingRepository;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public abstract class AbstractIT {

    static final KeycloakContainer KEYCLOAK;

    static final GenericContainer REDIS;

    static {
        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withReuse(true);

        REDIS = new GenericContainer("redis")
                .withExposedPorts(6379)
                .withReuse(true);

        KEYCLOAK.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void configDatasource(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "master");
        registry.add("app.keycloak.adminUsername", KEYCLOAK::getAdminUsername);
        registry.add("app.keycloak.adminPassword", KEYCLOAK::getAdminPassword);
    }

    @DynamicPropertySource
    static void configRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
        registry.add("spring.cache.type", () -> "redis");
    }

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FollowingRepository followingRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearRepository() {

        userProfileRepository.deleteAll();
        followingRepository.deleteAll();
    }

    @BeforeEach
    void clearCache() {

        cacheManager.getCacheNames().forEach(cache -> cacheManager.getCache(cache).invalidate());
    }
}
