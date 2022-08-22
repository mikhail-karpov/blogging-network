package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.PostCreatedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.PostDeletedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.PostStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
class PostEventNotificationServiceTest extends AbstractIT {

    @Autowired
    private NotificationService<PostEvent> notificationService;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.cloud.stream.bindings.postEvents-out-0.destination}")
    private String exchange;

    private String postCreatedQueue;
    private String postDeletedQueue;

    private Binding postCreatedBinding;
    private Binding postDeletedBinding;

    @BeforeEach
    void setUp() {
        amqpAdmin.declareExchange(new TopicExchange(exchange));
        postCreatedQueue = amqpAdmin.declareQueue(new Queue(UUID.randomUUID().toString()));
        postDeletedQueue = amqpAdmin.declareQueue(new Queue(UUID.randomUUID().toString()));

        postCreatedBinding = new Binding(
                postCreatedQueue,
                Binding.DestinationType.QUEUE,
                exchange,
                PostStatus.CREATED.name(),
                null
        );
        postDeletedBinding = new Binding(
                postDeletedQueue,
                Binding.DestinationType.QUEUE,
                exchange,
                PostStatus.DELETED.name(),
                null
        );

        amqpAdmin.declareBinding(postCreatedBinding);
        amqpAdmin.declareBinding(postDeletedBinding);
    }

    @AfterEach
    void tearDown() {
        amqpAdmin.removeBinding(postCreatedBinding);
        amqpAdmin.removeBinding(postDeletedBinding);
    }

    @Test
    void sendPostCreated() {
        //given
        PostEvent event = new PostCreatedEvent(UUID.randomUUID(), "authorId", "post content");

        //when
        notificationService.send(event);

        //then
        Message receive = rabbitTemplate.receive(postCreatedQueue, 1000L);
        assertNotNull(receive.getBody());
    }

    @Test
    void sendPostDeleted() {
        //given
        PostEvent event = new PostDeletedEvent(UUID.randomUUID(), "authorId");

        //when
        notificationService.send(event);

        //then
        Message receive = rabbitTemplate.receive(postDeletedQueue, 1000L);
        assertNotNull(receive.getBody());
    }
}