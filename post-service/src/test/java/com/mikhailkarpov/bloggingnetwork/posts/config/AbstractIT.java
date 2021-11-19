package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

public abstract class AbstractIT {

    static final PostgreSQLContainer POSTGRES;

    static final KeycloakContainer KEYCLOAK;

    static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static {
        POSTGRES = new PostgreSQLContainer("postgres")
                .withDatabaseName("post_service")
                .withUsername("post_service")
                .withPassword("password");

        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("/test-realm.json");

        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq")
                .withExposedPorts(5672);

        POSTGRES.start();
        KEYCLOAK.start();
        RABBIT_MQ_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configKeycloak(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "test");
        registry.add("app.keycloak.user", KEYCLOAK::getAdminUsername);
        registry.add("app.keycloak.password", KEYCLOAK::getAdminPassword);
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
    }

    @DynamicPropertySource
    static void configDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.driver-class-name", () -> POSTGRES.getDriverClassName());
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
    }

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void cleanRepository() {

        postRepository.deleteAll();
    }

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @AfterEach
    void purgeQueue() {
        this.rabbitAdmin.purgeQueue(RabbitMQConfig.POST_EVENT_QUEUE, true);
    }
}

