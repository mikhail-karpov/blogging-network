package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class PostDtoTest {

    @Autowired
    private JacksonTester<PostDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        PostDto postDto = PostDto.builder()
                .id("postId")
                .user(new UserProfileDto("user", "username"))
                .content("Post content")
                .createdDate(Instant.parse("2020-03-31T06:55:02.00Z"))
                .build();

        //when
        JsonContent<PostDto> json = jacksonTester.write(postDto);

        //then
        assertThat(json).extractingJsonPathStringValue("$.id").isEqualTo("postId");
        assertThat(json).extractingJsonPathStringValue("$.user.userId").isEqualTo("user");
        assertThat(json).extractingJsonPathStringValue("$.user.username").isEqualTo("username");
        assertThat(json).extractingJsonPathStringValue("$.content").isEqualTo("Post content");
        assertThat(json).extractingJsonPathStringValue("$.createdDate").isEqualTo("2020-03-31T06:55:02Z");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{" +
                "\"id\":\"postId\", " +
                "\"user\":{\"userId\":\"user-id\", \"username\":\"user\"}, " +
                "\"content\":\"Post content\" ," +
                "\"createdDate\":\"2020-03-31T06:55:02Z\"" +
                "}";

        //when
        PostDto dto = jacksonTester.parse(json).getObject();

        //then
        assertThat(dto.getId()).isEqualTo("postId");
        assertThat(dto.getUser().getUserId()).isEqualTo("user-id");
        assertThat(dto.getUser().getUsername()).isEqualTo("user");
        assertThat(dto.getContent()).isEqualTo("Post content");
        assertThat(dto.getCreatedDate()).isEqualTo(Instant.parse("2020-03-31T06:55:02.00Z"));
    }
}