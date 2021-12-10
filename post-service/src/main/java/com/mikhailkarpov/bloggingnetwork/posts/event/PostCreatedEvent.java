package com.mikhailkarpov.bloggingnetwork.posts.event;

public class PostCreatedEvent extends PostAbstractEvent {

    public PostCreatedEvent(String authorId, String postId) {
        super(authorId, postId, EventStatus.CREATED);
    }
}
