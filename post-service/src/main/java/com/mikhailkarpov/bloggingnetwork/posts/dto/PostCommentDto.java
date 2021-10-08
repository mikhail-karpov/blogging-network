package com.mikhailkarpov.bloggingnetwork.posts.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCommentDto {

    private String id;

    private String userId;

    private String comment;

    private LocalDateTime createdDate;

    @Builder
    public PostCommentDto(String id, String userId, String comment, LocalDateTime createdDate) {
        this.id = id;
        this.userId = userId;
        this.comment = comment;
        this.createdDate = createdDate;
    }
}
