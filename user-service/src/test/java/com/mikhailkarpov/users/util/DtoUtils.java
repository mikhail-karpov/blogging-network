package com.mikhailkarpov.users.util;

import com.mikhailkarpov.users.dto.SignUpRequest;

import java.util.UUID;

public class DtoUtils {

    public static SignUpRequest createRandomRequest() {
        String username = UUID.randomUUID().toString();
        String email = username + "@fake.com";
        String password = "password";

        return new SignUpRequest(username, email, password.toCharArray());
    }
}
