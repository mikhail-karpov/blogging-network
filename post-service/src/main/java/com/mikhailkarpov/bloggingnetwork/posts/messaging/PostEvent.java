package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class PostEvent {

    private final UUID postId;
    private final String authorId;
    private final String postContent;
    private final EventStatus status;
}
