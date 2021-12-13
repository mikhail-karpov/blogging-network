package com.mikhailkarpov.bloggingnetwork.posts.contract;

import com.mikhailkarpov.bloggingnetwork.posts.config.RabbitMQConfig;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.*;
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

import java.util.UUID;

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
    private AmqpPostEventPublisher amqpPostEventPublisher;

    public void sendPostCreatedEvent() {
        UUID postId = UUID.fromString("37e4e4d0-c615-4c5c-85b5-adab30093def");
        String postContent = "Post content";
        String authorId = "author-id";
        PostEvent event = new PostCreatedEvent(postId, authorId, postContent);

        this.amqpPostEventPublisher.publish(event);
    }

    public void sendPostDeletedEvent() {
        UUID postId = UUID.fromString("35cacb21-dc2a-4912-afe7-51d693e8f208");
        String authorId = "author-id";
        PostEvent event = new PostDeletedEvent(postId, authorId);
        this.amqpPostEventPublisher.publish(event);
    }

}
