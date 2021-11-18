package com.mikhailkarpov.bloggingnetwork.posts.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class PostCommentDto {

    private String id;
    private String comment;
    private Instant createdDate;
    private UserProfileDto user;

    @Builder
    public PostCommentDto(String id, UserProfileDto user, String comment, Instant createdDate) {
        this.id = id;
        this.user = user;
        this.comment = comment;
        this.createdDate = createdDate;
    }
}
