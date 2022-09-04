package com.mikhailkarpov.bloggingnetwork.feed.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent {

    public enum Status {
        CREATED, DELETED
    }

    private String authorId;

    private String postId;

    private String content;

    private Status status;
}
