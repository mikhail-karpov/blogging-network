package com.mikhailkarpov.bloggingnetwork.feed.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;

public class AbstractIT {

    static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static final KeycloakContainer KEYCLOAK;

    static {
        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq");
        RABBIT_MQ_CONTAINER.start();

        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2");
        KEYCLOAK.start();
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
    }

    @DynamicPropertySource
    static void configJwtIssuer(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> String.format("%s/realms/master", KEYCLOAK.getAuthServerUrl()));
    }
}
