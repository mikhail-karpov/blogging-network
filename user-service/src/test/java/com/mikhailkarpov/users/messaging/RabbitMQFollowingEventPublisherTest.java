package com.mikhailkarpov.users.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;

@ExtendWith(MockitoExtension.class)
class RabbitMQFollowingEventPublisherTest {

    private static final String EXCHANGE = "exchange";
    private static final String FOLLOWED_KEY = "followed";
    private static final String UNFOLLOWED_KEY = "unfollowed";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQFollowingEventPublisher messageSender;

    @BeforeEach
    void setUp() {
        this.messageSender = new RabbitMQFollowingEventPublisher(
                this.rabbitTemplate, EXCHANGE, FOLLOWED_KEY, UNFOLLOWED_KEY);
    }

    @Test
    void givenFollowedEvent_whenPublish_thenMessageSent() {
        //given
        FollowingEvent event = new FollowingEvent("follower", "following", FOLLOWED);

        //when
        this.messageSender.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(EXCHANGE, FOLLOWED_KEY, event);
    }

    @Test
    void givenUnfollowedEvent_whenPublish_thenMessageSent() {
        //given
        FollowingEvent event = new FollowingEvent("follower", "following", UNFOLLOWED);

        //when
        this.messageSender.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(EXCHANGE, UNFOLLOWED_KEY, event);
    }
}