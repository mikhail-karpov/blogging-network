package com.mikhailkarpov.bloggingnetwork.posts.contract;

import com.mikhailkarpov.bloggingnetwork.posts.config.RabbitMQConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostCreatedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostDeletedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostAbstractEvent;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.AmqpPostMessagePublisher;
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
    private AmqpPostMessagePublisher amqpPostMessagePublisher;

    private final Post post = new Post("author-id", "post-id");

    public void sendPostCreatedEvent() {
        PostAbstractEvent event = new PostCreatedEvent("author-id", "post-id");
        this.amqpPostMessagePublisher.publish(event);
    }

    public void sendPostDeletedEvent() {
        PostAbstractEvent event = new PostDeletedEvent("author-id", "post-id");
        this.amqpPostMessagePublisher.publish(event);
    }

}
