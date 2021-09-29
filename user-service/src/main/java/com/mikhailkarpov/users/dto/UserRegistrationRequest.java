package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
public class UserRegistrationRequest {

    @JsonProperty("username")
    @NotBlank(message = "Username must be provided")
    private final String username;

    @JsonProperty("email")
    @NotBlank(message = "Email must be provided")
    @Email
    private final String email;

    @JsonProperty("password")
    @NotBlank(message = "Password must be provided")
    private final String password;

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "[SECURED]" + '\'' +
                '}';
    }
}
