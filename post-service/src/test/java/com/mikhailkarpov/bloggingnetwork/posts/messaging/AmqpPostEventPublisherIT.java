package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.config.RabbitMQConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitMQConfig.class,
        RabbitAutoConfiguration.class,
        AmqpPostEventPublisherIT.TestConfig.class})
@Testcontainers
class AmqpPostEventPublisherIT {

    @TestConfiguration
    @RabbitListenerTest
    public static class TestConfig {

        @Component
        public class TestListener {

            private static final String LISTENER_ID = "post-event-listener";

            @RabbitListener(id = LISTENER_ID, queues = "post-event-queue")
            public void handle(PostEvent event) {
                //do nothing
            }
        }
    }

    @Container
    static RabbitMQContainer RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq");

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
    }

    @Autowired
    private AmqpPostEventPublisher eventPublisher;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Test
    void testPublish() throws InterruptedException {
        TestConfig.TestListener listener = this.harness.getSpy(TestConfig.TestListener.LISTENER_ID);
        assertNotNull(listener);

        LatchCountDownAndCallRealMethodAnswer answer =
                this.harness.getLatchAnswerFor(TestConfig.TestListener.LISTENER_ID, 2);
        doAnswer(answer).when(listener).handle(any());

        this.eventPublisher.publish(
                new PostCreatedEvent(UUID.randomUUID(), "author-id", "Post content"));

        this.eventPublisher.publish(
                new PostDeletedEvent(UUID.randomUUID(), "author-id"));

        assertTrue(answer.await(30));
        verify(listener).handle(any(PostCreatedEvent.class));
        verify(listener).handle(any(PostDeletedEvent.class));
    }
}