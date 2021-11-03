package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostEvent {

    private String postId;
    private String authorId;
    private EventStatus status;

    @Builder
    public PostEvent(String postId, String authorId, EventStatus status) {
        this.postId = postId;
        this.authorId = authorId;
        this.status = status;
    }
}
