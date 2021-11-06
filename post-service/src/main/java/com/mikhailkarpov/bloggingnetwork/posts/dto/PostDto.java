package com.mikhailkarpov.bloggingnetwork.posts.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {

    private final String id;

    private final String content;

    private final LocalDateTime createdDate;

    private final UserProfileDto user;

    @Builder
    public PostDto(String id, UserProfileDto user, String content, LocalDateTime createdDate) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.createdDate = createdDate;
    }
}
