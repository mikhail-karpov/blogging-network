package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;

public abstract class AbstractIT {

    static final KeycloakContainer KEYCLOAK;

    static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static {
        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2");
        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);

        KEYCLOAK.start();
        RABBIT_MQ_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configKeycloak(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "master");
        registry.add("app.keycloak.user", KEYCLOAK::getAdminUsername);
        registry.add("app.keycloak.password", KEYCLOAK::getAdminPassword);
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
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

