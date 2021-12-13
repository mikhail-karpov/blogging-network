package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.event.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostCreatedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostDeletedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostAbstractEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AmqpPostMessagePublisherTest {

    private final RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
    private final String exchange = "posts-exchange";
    private final String postCreatedKey = "post.created";
    private final String postDeletedKey = "post.deleted";

    private final AmqpPostMessagePublisher messagePublisher = new AmqpPostMessagePublisher(
            rabbitTemplate, exchange, postCreatedKey, postDeletedKey);

    @Captor
    private ArgumentCaptor<PostMessage> messageArgumentCaptor;

    @Test
    void shouldPublishPostCreatedMessage() {
        //when
        PostAbstractEvent event = new PostCreatedEvent("author-id", "post-id");
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate)
                .convertAndSend(eq(this.exchange), eq(this.postCreatedKey), this.messageArgumentCaptor.capture());

        PostMessage message = this.messageArgumentCaptor.getValue();
        assertEquals("author-id", message.getAuthorId());
        assertEquals("post-id", message.getPostId());
        assertEquals(EventStatus.CREATED, message.getStatus());
    }

    @Test
    void shouldPublishPostDeletedMessage() {
        //when
        PostAbstractEvent event = new PostDeletedEvent("author-id", "post-id");
        this.messagePublisher.publish(event);

        //then
        verify(this.rabbitTemplate)
                .convertAndSend(eq(this.exchange), eq(this.postDeletedKey), this.messageArgumentCaptor.capture());

        PostMessage message = this.messageArgumentCaptor.getValue();
        assertEquals("author-id", message.getAuthorId());
        assertEquals("post-id", message.getPostId());
        assertEquals(EventStatus.DELETED, message.getStatus());
    }
}