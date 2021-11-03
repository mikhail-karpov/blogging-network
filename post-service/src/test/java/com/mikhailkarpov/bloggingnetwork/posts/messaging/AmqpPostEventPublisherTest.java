package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.config.messaging.MessagingProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AmqpPostEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessagingProperties properties;

    @InjectMocks
    private AmqpPostEventPublisher eventPublisher;

    @Test
    void shouldPublishPostCreatedEvent() {
        //given
        String exchange = "posts-exchange";
        String key = "post.created";

        Mockito.when(this.properties.getTopicExchange()).thenReturn(exchange);
        Mockito.when(this.properties.getPostCreatedRoutingKey()).thenReturn(key);

        //when
        PostEvent event = new PostEvent("postId", "authorId", EventStatus.CREATED);
        eventPublisher.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(exchange, key, event);
    }

    @Test
    void shouldPublishPostDeletedEvent() {
        //given
        String exchange = "posts-exchange";
        String key = "post.created";

        Mockito.when(this.properties.getTopicExchange()).thenReturn(exchange);
        Mockito.when(this.properties.getPostDeletedRoutingKey()).thenReturn(key);

        //when
        PostEvent event = new PostEvent("postId", "authorId", EventStatus.DELETED);
        this.eventPublisher.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(exchange, key, event);
    }
}