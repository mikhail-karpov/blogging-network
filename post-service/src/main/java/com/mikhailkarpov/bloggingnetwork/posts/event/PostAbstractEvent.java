package com.mikhailkarpov.bloggingnetwork.posts.event;

import lombok.Data;

@Data
public abstract class PostAbstractEvent {

    private final String authorId;
    private final String postId;
    private final EventStatus status;
}
