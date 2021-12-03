package com.mikhailkarpov.bloggingnetwork.posts.domain;

import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreRemove;

@Component
@RequiredArgsConstructor
public class PostAuditListener {

    private final PostEventPublisher eventPublisher;

    @PostPersist
    public void sendPostCreatedEvent(Post post) {
        PostEvent event = new PostEvent(post.getId().toString(), post.getUserId(), EventStatus.CREATED);
        eventPublisher.publish(event);
    }

    @PostRemove
    public void sendPostDeletedEvent(Post post) {
        PostEvent event = new PostEvent(post.getId().toString(), post.getUserId(), EventStatus.DELETED);
        eventPublisher.publish(event);
    }
}
