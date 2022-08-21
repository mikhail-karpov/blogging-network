package com.mikhailkarpov.users.service.impl;

import com.mikhailkarpov.users.AbstractIT;
import com.mikhailkarpov.users.dto.FollowingNotification;
import com.mikhailkarpov.users.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static com.mikhailkarpov.users.dto.FollowingNotification.Status.FOLLOWED;
import static com.mikhailkarpov.users.dto.FollowingNotification.Status.UNFOLLOWED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
class FollowingNotificationServiceTest extends AbstractIT {

    @Autowired
    private NotificationService<FollowingNotification> notificationService;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.cloud.stream.bindings.userFollowing-out-0.destination}")
    private String exchange;

    private Queue followedQueue;
    private Queue unfollowedQueue;
    private Binding followedBinding;
    private Binding unfollowedBinding;

    @BeforeEach
    void setUp() {
        followedQueue = new Queue(UUID.randomUUID().toString());
        unfollowedQueue = new Queue(UUID.randomUUID().toString());

        followedBinding = new Binding(
                followedQueue.getName(),
                Binding.DestinationType.QUEUE,
                exchange,
                "FOLLOWED",
                null
        );

        unfollowedBinding = new Binding(
                unfollowedQueue.getName(),
                Binding.DestinationType.QUEUE,
                exchange,
                "UNFOLLOWED",
                null
        );

        amqpAdmin.declareExchange(new TopicExchange(exchange));
        amqpAdmin.declareQueue(followedQueue);
        amqpAdmin.declareQueue(unfollowedQueue);
        amqpAdmin.declareBinding(followedBinding);
        amqpAdmin.declareBinding(unfollowedBinding);
    }

    @Test
    void sendFollowingNotification() {

    FollowingNotification notification =
            new FollowingNotification("followerId", "followingId", FOLLOWED);

        notificationService.send(notification);
        Message receivedMessage = rabbitTemplate.receive(followedQueue.getName(), 1000L);

        assertNotNull(receivedMessage.getBody());
    }

    @Test
    void sendUnfollowingNotification() {

        FollowingNotification notification =
                new FollowingNotification("followerId", "followingId", UNFOLLOWED);

        notificationService.send(notification);
        Message receivedMessage = rabbitTemplate.receive(unfollowedQueue.getName(), 1000L);

        assertNotNull(receivedMessage.getBody());
    }
}