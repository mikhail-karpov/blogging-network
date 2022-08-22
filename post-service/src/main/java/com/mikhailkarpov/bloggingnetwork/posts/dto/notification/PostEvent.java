package com.mikhailkarpov.bloggingnetwork.posts.dto.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class PostEvent implements Notification {

    private final UUID postId;
    private final String authorId;
    private final String postContent;
    private final PostStatus status;

    @Override
    public final String getType() {
        return status.name();
    }

}
