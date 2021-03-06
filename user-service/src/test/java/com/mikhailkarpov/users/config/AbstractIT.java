package com.mikhailkarpov.users.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

public abstract class AbstractIT {

    static final PostgreSQLContainer POSTGRES;

    static final KeycloakContainer KEYCLOAK;

    static final GenericContainer REDIS;

    static final RabbitMQContainer RABBIT_MQ;

    static {
        POSTGRES = new PostgreSQLContainer("postgres")
                .withDatabaseName("user_service")
                .withUsername("user_service")
                .withPassword("password");

        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("./bloggingnetwork-realm.json");

        REDIS = new GenericContainer("redis").withExposedPorts(6379);

        RABBIT_MQ = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);

        POSTGRES.start();
        KEYCLOAK.start();
        REDIS.start();
        RABBIT_MQ.start();
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

    @DynamicPropertySource
    static void configDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.driver-class-name", () -> POSTGRES.getDriverClassName());
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", () -> RABBIT_MQ.getHost());
        registry.add("spring.rabbitmq.port", () -> RABBIT_MQ.getAmqpPort());
    }
}
