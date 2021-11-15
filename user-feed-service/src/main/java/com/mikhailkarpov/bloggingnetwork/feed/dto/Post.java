package com.mikhailkarpov.bloggingnetwork.feed.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Post {

    private String id;

    private String content;

    private UserProfile user;

    private LocalDateTime createdDate;

    @Builder
    public Post(String id, String content, UserProfile user, LocalDateTime createdDate) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.createdDate = createdDate;
    }
}
