package com.mikhailkarpov.users.messaging;

import com.mikhailkarpov.users.config.MessagingConfig;
import com.mikhailkarpov.users.config.RabbitListenerTestConfig;
import com.mikhailkarpov.users.config.RabbitListenerTestConfig.TestListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        MessagingConfig.class,
        RabbitListenerTestConfig.class
})
@Testcontainers
class RabbitMQFollowingMessageSenderIT {

    @Container
    static RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq");

    @DynamicPropertySource
    static void configRabbit(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @Autowired
    private RabbitMQFollowingMessageSender messageSender;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Test
    void testPublish() throws InterruptedException {
        TestListener listener = this.harness.getSpy(TestListener.LISTENER_ID);
        assertThat(listener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer =
                this.harness.getLatchAnswerFor(TestListener.LISTENER_ID, 2);
        doAnswer(answer).when(listener).handle(any());

        this.messageSender.publish(new FollowingEvent("followerId", "followingId", Status.FOLLOWED));
        this.messageSender.publish(new FollowingEvent("followerId", "followingId", Status.UNFOLLOWED));

        assertThat(answer.await(30)).isTrue();
        verify(listener, times(2)).handle(any());
    }
}