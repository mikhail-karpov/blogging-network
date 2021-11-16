package com.mikhailkarpov.bloggingnetwork.feed.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfile {

    private String userId;

    private String username;

    public UserProfile(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
