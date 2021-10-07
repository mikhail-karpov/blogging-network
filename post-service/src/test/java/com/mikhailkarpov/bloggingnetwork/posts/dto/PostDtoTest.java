package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class PostDtoTest {

    @Autowired
    private JacksonTester<PostDto> jacksonTester;
    private final LocalDateTime dateTime =
            LocalDateTime.of(2020, 3, 31, 6, 55, 2);

    @Test
    void testSerialize() throws IOException {
        //given
        PostDto postDto = PostDto.builder()
                .id("postId")
                .userId("user")
                .content("Post content")
                .createdDate(dateTime)
                .build();

        //when
        JsonContent<PostDto> json = jacksonTester.write(postDto);

        //then
        assertThat(json).extractingJsonPathStringValue("$.id").isEqualTo("postId");
        assertThat(json).extractingJsonPathStringValue("$.userId").isEqualTo("user");
        assertThat(json).extractingJsonPathStringValue("$.content").isEqualTo("Post content");
        assertThat(json).extractingJsonPathStringValue("$.createdDate").isEqualTo("2020-03-31T06:55:02");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{" +
                "\"id\":\"postId\", " +
                "\"userId\":\"user\", " +
                "\"content\":\"Post content\" ," +
                "\"createdDate\":\"2020-03-31T06:55:02\"" +
                "}";

        //when
        PostDto dto = jacksonTester.parse(json).getObject();

        //then
        assertThat(dto.getId()).isEqualTo("postId");
        assertThat(dto.getUserId()).isEqualTo("user");
        assertThat(dto.getContent()).isEqualTo("Post content");
        assertThat(dto.getCreatedDate()).isEqualTo(dateTime);
    }
}