package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoTest {

    @Autowired
    private JacksonTester<CommentDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        String id = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        UserProfileDto user = new UserProfileDto(userId, "username");
        CommentDto dto = new CommentDto(id, user, "Post comment", Instant.parse("2020-02-15T01:45:33.00Z"));

        //when
        JsonContent<CommentDto> json = jacksonTester.write(dto);

        //then
        assertThat(json).extractingJsonPathStringValue("$.id").isEqualTo(id);
        assertThat(json).extractingJsonPathStringValue("$.comment").isEqualTo("Post comment");
        assertThat(json).extractingJsonPathStringValue("$.createdDate").isEqualTo("2020-02-15T01:45:33Z");
        assertThat(json).extractingJsonPathStringValue("$.user.userId").isEqualTo(userId);
        assertThat(json).extractingJsonPathStringValue("$.user.username").isEqualTo("username");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{" +
                "\"id\":\"commentId\", " +
                "\"user\":{\"userId\":\"user-id\",\"username\":\"name\"}," +
                "\"comment\":\"Post comment\", " +
                "\"createdDate\":\"2002-04-13T01:05:13Z\"" +
                "}";

        //when
        CommentDto dto = jacksonTester.parse(json).getObject();

        //then
        assertThat(dto.getId()).isEqualTo("commentId");
        assertThat(dto.getComment()).isEqualTo("Post comment");
        assertThat(dto.getCreatedDate()).isEqualTo(Instant.parse("2002-04-13T01:05:13Z"));
        assertThat(dto.getUser().getUserId()).isEqualTo("user-id");
        assertThat(dto.getUser().getUsername()).isEqualTo("name");
    }
}