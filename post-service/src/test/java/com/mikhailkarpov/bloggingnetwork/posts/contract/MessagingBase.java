package com.mikhailkarpov.bloggingnetwork.posts.contract;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.AmqpPostEventPublisher;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;

@SpringBootTest(
        properties = {
                "stubrunner.amqp.enabled=true",
                "stubrunner.amqp.mockConnection=false",
        })
@AutoConfigureMessageVerifier
public class MessagingBase extends AbstractIT {

    @Autowired
    private AmqpPostEventPublisher eventPublisher;

    public void sendPostCreatedEvent() {
        PostEvent event = new PostEvent("post-id", "author-id", EventStatus.CREATED);
        this.eventPublisher.publish(event);
    }

    public void sendPostDeletedEvent() {
        PostEvent event = new PostEvent("post-id", "author-id", EventStatus.DELETED);
        this.eventPublisher.publish(event);
    }

    @Test
    void contextLoads() {
        Assertions.assertNotNull(this.eventPublisher);
    }
}
