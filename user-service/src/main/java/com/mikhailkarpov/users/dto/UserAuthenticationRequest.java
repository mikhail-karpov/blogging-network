package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class UserAuthenticationRequest {

    @NotBlank(message = "Username must be provided")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Password must be provided")
    @JsonProperty("password")
    private String password;
}
