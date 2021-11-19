package com.mikhailkarpov.bloggingnetwork.feed.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

public class AbstractIT {

    static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static final KeycloakContainer KEYCLOAK;

    static final PostgreSQLContainer POSTGRES;

    static {
        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq");

        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("/userfeed-realm.json");

        POSTGRES = new PostgreSQLContainer<>("postgres")
                .withDatabaseName("user_feed_service")
                .withUsername("user_feed_service")
                .withPassword("pa55word")
                .withExposedPorts(5432);

        RABBIT_MQ_CONTAINER.start();
        KEYCLOAK.start();
        POSTGRES.start();
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
    private static void configDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
        registry.add("spring.datasource.driver-class-name", () -> POSTGRES.getDriverClassName());
    }
}
