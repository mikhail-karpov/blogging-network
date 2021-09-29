package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class UserProfileDto {

    @JsonProperty("userId")
    private final String id;

    @JsonProperty("username")
    private final String username;
}
