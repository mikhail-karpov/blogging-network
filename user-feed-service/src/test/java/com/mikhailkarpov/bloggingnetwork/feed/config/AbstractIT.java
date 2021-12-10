package com.mikhailkarpov.bloggingnetwork.feed.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;

public class AbstractIT {

    static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static final KeycloakContainer KEYCLOAK;

    static final GenericContainer REDIS;

    static {
        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq");

        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("/userfeed-realm.json");

        REDIS = new GenericContainer("redis:latest").withExposedPorts(6379);

        RABBIT_MQ_CONTAINER.start();
        KEYCLOAK.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
    }

    @DynamicPropertySource
    static void configJwtIssuer(DynamicPropertyRegistry registry) {
        registry.add("keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("keycloak.realm", () -> "userfeed");
    }

    @DynamicPropertySource
    static void configRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
    }
}
