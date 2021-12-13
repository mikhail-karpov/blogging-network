package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AmqpPostEventPublisherTest {

    private final RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
    private final String exchange = "posts-exchange";
    private final String postCreatedKey = "post.created";
    private final String postDeletedKey = "post.deleted";

    private final AmqpPostEventPublisher messagePublisher = new AmqpPostEventPublisher(
            rabbitTemplate, exchange, postCreatedKey, postDeletedKey);

    @Test
    void shouldPublishPostCreatedEvent() {
        //when
        PostEvent event = new PostEvent("author-id", "post-id", EventStatus.CREATED);
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate).convertAndSend(this.exchange, this.postCreatedKey, event);
    }

    @Test
    void shouldPublishPostDeletedEvent() {
        //when
        PostEvent event = new PostEvent("author-id", "post-id", EventStatus.DELETED);
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate).convertAndSend(this.exchange, this.postDeletedKey, event);
    }
}