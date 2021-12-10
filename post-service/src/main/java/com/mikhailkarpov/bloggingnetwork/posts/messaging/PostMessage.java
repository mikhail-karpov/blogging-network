package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.event.EventStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostMessage {

    private String postId;
    private String authorId;
    private EventStatus status;

    @Builder
    public PostMessage(String postId, String authorId, EventStatus status) {
        this.postId = postId;
        this.authorId = authorId;
        this.status = status;
    }
}
