package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mikhailkarpov.users.domain.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    @JsonProperty("userId")
    private String id;

    @JsonProperty("username")
    private String username;

    public UserProfileDto(UserProfile profile) {
        this.id = profile.getId();
        this.username = profile.getUsername();
    }
}
