package com.mikhailkarpov.users.util;

import com.mikhailkarpov.users.dto.UserRegistrationRequest;

import java.util.UUID;

public class DtoUtils {

    public static UserRegistrationRequest createRandomRequest() {
        String username = UUID.randomUUID().toString();
        String email = username + "@fake.com";
        String password = "password";

        return new UserRegistrationRequest(username, email, password);
    }
}
