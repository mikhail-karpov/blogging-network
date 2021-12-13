package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

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
        UUID postId = UUID.fromString("37e4e4d0-c615-4c5c-85b5-adab30093def");
        String postContent = "Post content";
        String authorId = "author-id";
        PostEvent event = new PostCreatedEvent(postId, authorId, postContent);
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate).convertAndSend(this.exchange, this.postCreatedKey, event);
    }

    @Test
    void shouldPublishPostDeletedEvent() {
        //when
        UUID postId = UUID.fromString("35cacb21-dc2a-4912-afe7-51d693e8f208");
        String authorId = "author-id";
        PostEvent event = new PostDeletedEvent(postId, authorId);
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate).convertAndSend(this.exchange, this.postDeletedKey, event);
    }
}