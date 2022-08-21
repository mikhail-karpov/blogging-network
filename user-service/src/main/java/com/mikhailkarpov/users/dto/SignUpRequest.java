package com.mikhailkarpov.users.dto;

import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Value
public class SignUpRequest {

    @NotBlank
    String username;

    @Email
    @NotBlank
    String email;

    @NotEmpty
    @ToString.Exclude
    char[] password;

}
