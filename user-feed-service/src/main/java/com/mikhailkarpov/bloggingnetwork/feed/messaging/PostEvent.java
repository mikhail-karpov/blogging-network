package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostEvent {

    public enum Status {
        CREATED, DELETED
    }

    private String authorId;

    private String postId;

    private Status status;

}
