package com.mikhailkarpov.bloggingnetwork.posts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class CreateCommentRequest {

    @NotBlank(message = "Comment must be provided")
    @Size(min = 4, max = 180, message = "Comment must be between 4 and 180 characters long")
    private final String comment;

    public CreateCommentRequest(@JsonProperty("comment") String comment) {
        this.comment = comment;
    }
}
