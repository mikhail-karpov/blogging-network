package com.mikhailkarpov.bloggingnetwork.posts.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class PostCreatedEvent extends PostEvent {

    @JsonCreator
    public PostCreatedEvent(@JsonProperty("postId") UUID postId,
                            @JsonProperty("authorId") String authorId,
                            @JsonProperty("postContent") String postContent) {
        super(postId, authorId, postContent, PostStatus.CREATED);
    }
}
