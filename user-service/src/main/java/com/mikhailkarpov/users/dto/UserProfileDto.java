package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mikhailkarpov.users.domain.UserProfile;
import lombok.Value;

@Value
public class UserProfileDto {

    @JsonProperty("userId")
    private String id;

    @JsonProperty("username")
    private String username;

    public static UserProfileDto from(UserProfile profile) {
        return new UserProfileDto(profile.getId(), profile.getUsername());
    }
}
