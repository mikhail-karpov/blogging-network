package com.mikhailkarpov.users.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserProfileDtoTest {

    @Autowired
    private JacksonTester<UserProfileDto> profileDtoJson;

    private final String id = UUID.randomUUID().toString();
    private final UserProfileDto profileDto = new UserProfileDto(id, "username");

    @Test
    void testSerialize() throws IOException {
        //when
        JsonContent<UserProfileDto> json = profileDtoJson.write(profileDto);

        //then
        assertThat(json).extractingJsonPathStringValue("$.userId").isEqualTo(id);
        assertThat(json).extractingJsonPathStringValue("$.username").isEqualTo("username");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = String.format("{\"id\":\"%s\", \"username\":\"username\"}", id);

        //when
        UserProfileDto profileDto = profileDtoJson.parse(json).getObject();

        //then
        assertThat(profileDto.getId()).isEqualTo(id);
        assertThat(profileDto.getUsername()).isEqualTo("username");
    }
}