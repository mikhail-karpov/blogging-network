package com.mikhailkarpov.bloggingnetwork.posts.event;

public class PostDeletedEvent extends PostAbstractEvent {

    public PostDeletedEvent(String authorId, String postId) {
        super(authorId, postId, EventStatus.DELETED);
    }
}
