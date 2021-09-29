package com.mikhailkarpov.users.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserRegistrationRequestTest {

    @Autowired
    private JacksonTester<UserRegistrationRequest> requestTester;

    @Test
    void testSerialize() throws IOException {
        //given
        String username = "user";
        String email = "user@example.com";
        String password = "pass";
        UserRegistrationRequest request = new UserRegistrationRequest(username, email, password);

        //when
        JsonContent<UserRegistrationRequest> json = requestTester.write(request);

        //then
        assertThat(json).extractingJsonPathStringValue("$.username").isEqualTo(username);
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo(email);
        assertThat(json).extractingJsonPathStringValue("$.password").isEqualTo(password);
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{\"username\":\"user\", \"email\":\"user@example.com\", \"password\":\"pass\"}";

        //when
        UserRegistrationRequest request = requestTester.parse(json).getObject();

        //then
        assertThat(request.getUsername()).isEqualTo("user");
        assertThat(request.getEmail()).isEqualTo("user@example.com");
        assertThat(request.getPassword()).isEqualTo("pass");
    }
}