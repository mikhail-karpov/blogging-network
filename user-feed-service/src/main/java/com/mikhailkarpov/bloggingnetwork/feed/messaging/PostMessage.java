package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMessage {

    public enum Status {
        CREATED, DELETED
    }

    private String authorId;

    private String postId;

    private String content;

    private Status status;
}
