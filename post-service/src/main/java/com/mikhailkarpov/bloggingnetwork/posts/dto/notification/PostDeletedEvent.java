package com.mikhailkarpov.bloggingnetwork.posts.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class PostDeletedEvent extends PostEvent {

    @JsonCreator
    public PostDeletedEvent(@JsonProperty("postId") UUID postId,
                            @JsonProperty("authorId") String authorId) {
        super(postId, authorId, null, PostStatus.DELETED);
    }
}
