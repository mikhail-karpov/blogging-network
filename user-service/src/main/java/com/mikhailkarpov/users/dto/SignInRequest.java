package com.mikhailkarpov.users.dto;

import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Value
public class SignInRequest {

    @NotBlank
    String username;

    @NotEmpty
    @ToString.Exclude
    char[] password;
}
