package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UserProfileDto {

    @JsonProperty("userId")
    private String id;

    @JsonProperty("username")
    private String username;
}
