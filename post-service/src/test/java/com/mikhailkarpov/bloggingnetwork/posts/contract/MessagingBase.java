package com.mikhailkarpov.bloggingnetwork.posts.contract;

import com.mikhailkarpov.bloggingnetwork.posts.config.RabbitMQConfig;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.AmqpPostEventPublisher;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        RabbitMQConfig.class
})
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false",
})
@AutoConfigureMessageVerifier
@Testcontainers
public class MessagingBase {

    @Container
    static RabbitMQContainer RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq")
            .withExposedPorts(5672);

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
    }

    @Autowired
    private AmqpPostEventPublisher eventPublisher;

    public void sendPostCreatedEvent() {
        PostEvent event = new PostEvent("post-id", "author-id", EventStatus.CREATED);
        this.eventPublisher.publish(event);
    }

    public void sendPostDeletedEvent() {
        PostEvent event = new PostEvent("post-id", "author-id", EventStatus.DELETED);
        this.eventPublisher.publish(event);
    }

}
