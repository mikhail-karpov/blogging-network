package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostEvent {

    public enum Status {
        CREATED, DELETED
    }

    @JsonProperty("postId")
    private String postId;

    @JsonProperty("authorId")
    private String authorId;

    @JsonProperty("status")
    private Status status;
}
