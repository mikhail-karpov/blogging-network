package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;

public abstract class AbstractRabbitMQContainer {

    static final RabbitMQContainer RABBIT_MQ;

    static {
        RABBIT_MQ = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);
        RABBIT_MQ.start();
    }

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }
}
