package com.mikhailkarpov.bloggingnetwork.posts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserProfileDto {

    private final String userId;
    private final String username;

    public UserProfileDto(@JsonProperty("userId") String userId, @JsonProperty("username") String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserProfileDto{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
