package com.mikhailkarpov.users.contract;

import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ContextConfiguration(classes = SecurityTestConfig.class)
@SpringBootTest(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false",
        "spring.main.allow-bean-definition-overriding=true"
})
@Testcontainers
@AutoConfigureMessageVerifier
public class MessagingBase {

    @Container
    static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq");

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
}
