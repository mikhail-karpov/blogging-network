package com.mikhailkarpov.users.contract;

import com.mikhailkarpov.users.config.MessagingConfig;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.RabbitMQFollowingEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        MessagingConfig.class
})
@AutoConfigureMessageVerifier
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false"
})
@Testcontainers
public class MessagingBase {

    @Container
    static RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @Autowired
    private RabbitMQFollowingEventPublisher eventPublisher;

    public void sendFollowingEvent() {
        FollowingEvent.Status status = FollowingEvent.Status.FOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", status);

        this.eventPublisher.publish(event);
    }

    public void sendUnfollowingEvent() {
        FollowingEvent.Status status = FollowingEvent.Status.UNFOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", status);

        this.eventPublisher.publish(event);
    }

    @Test
    void contextLoads() {
        Assertions.assertNotNull(this.eventPublisher);
    }
}
