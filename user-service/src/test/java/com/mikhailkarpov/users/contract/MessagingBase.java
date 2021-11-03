package com.mikhailkarpov.users.contract;

import com.mikhailkarpov.users.config.MessagingConfig;
import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
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
        MessagingConfig.class,
})
@AutoConfigureMessageVerifier
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false"
})
@Testcontainers
public class MessagingBase {

    @Container
    static RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq");

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @Autowired
    private FollowingEventPublisher eventPublisher;

    public void sendFollowingEvent() {
        FollowingEvent.Status eventType = FollowingEvent.Status.FOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", eventType);

        this.eventPublisher.publish(event);
    }

    public void sendUnfollowingEvent() {
        FollowingEvent.Status eventType = FollowingEvent.Status.UNFOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", eventType);

        this.eventPublisher.publish(event);
    }

    @Test
    void contextLoads() {
        Assertions.assertThat(this.eventPublisher).isNotNull();
    }
}
