package com.mikhailkarpov.users.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserAuthenticationRequestTest {

    @Autowired
    private JacksonTester<UserAuthenticationRequest> jsonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        UserAuthenticationRequest request = new UserAuthenticationRequest("user", "pass");

        //when
        JsonContent<UserAuthenticationRequest> jsonContent = jsonTester.write(request);

        //then
        assertThat(jsonContent).extractingJsonPathStringValue("$.username").isEqualTo("user");
        assertThat(jsonContent).extractingJsonPathStringValue("$.password").isEqualTo("pass");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{\"username\":\"user\", \"password\":\"pass\"}";

        //when
        UserAuthenticationRequest request = jsonTester.parse(json).getObject();

        //then
        assertThat(request.getUsername()).isEqualTo("user");
        assertThat(request.getPassword()).isEqualTo("pass");
    }
}