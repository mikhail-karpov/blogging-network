package com.mikhailkarpov.bloggingnetwork.posts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class CreatePostRequest {

    @NotBlank(message = "Post content must be provided")
    @Size(min = 4, max = 180, message = "Post content must be between 4 to 180 characters long")
    private String content;

    public CreatePostRequest(@JsonProperty("content") String content) {
        this.content = content;
    }
}
