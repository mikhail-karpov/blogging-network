package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class AmqpPostEventPublisherTest {

    private final RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
    private final String exchange = "posts-exchange";
    private final String postCreatedKey = "post.created";
    private final String postDeletedKey = "post.deleted";

    private final AmqpPostEventPublisher eventPublisher = new AmqpPostEventPublisher(
            rabbitTemplate, exchange, postCreatedKey, postDeletedKey);

    @Test
    void shouldPublishPostCreatedEvent() {
        //given
        PostEvent event = new PostEvent("postId", "authorId", EventStatus.CREATED);

        //when
        eventPublisher.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(exchange, postCreatedKey, event);
    }

    @Test
    void shouldPublishPostDeletedEvent() {
        //given
        PostEvent event = new PostEvent("postId", "authorId", EventStatus.DELETED);

        //when
        this.eventPublisher.publish(event);

        //then
        Mockito.verify(this.rabbitTemplate).convertAndSend(exchange, postDeletedKey, event);
    }
}