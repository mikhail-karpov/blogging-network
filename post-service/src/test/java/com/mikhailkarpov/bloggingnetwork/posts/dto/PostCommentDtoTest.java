package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class PostCommentDtoTest {

    @Autowired
    private JacksonTester<PostCommentDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        String id = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String comment = "Post comment";
        LocalDateTime createdDate = LocalDateTime.of(2020, 2, 15, 1, 45, 33);
        PostCommentDto dto = new PostCommentDto(id, userId, comment, createdDate);

        //when
        JsonContent<PostCommentDto> json = jacksonTester.write(dto);

        //then
        assertThat(json).extractingJsonPathStringValue("$.id").isEqualTo(id);
        assertThat(json).extractingJsonPathStringValue("$.userId").isEqualTo(userId);
        assertThat(json).extractingJsonPathStringValue("$.comment").isEqualTo("Post comment");
        assertThat(json).extractingJsonPathStringValue("$.createdDate").isEqualTo("2020-02-15T01:45:33");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{" +
                "\"id\":\"commentId\", " +
                "\"userId\":\"user\", " +
                "\"comment\":\"Post comment\", " +
                "\"createdDate\":\"2002-04-13T01:05:13\"" +
                "}";
        LocalDateTime expectedTime = LocalDateTime.of(2002, 4, 13, 1, 5, 13);

        //when
        PostCommentDto dto = jacksonTester.parse(json).getObject();

        //then
        assertThat(dto.getId()).isEqualTo("commentId");
        assertThat(dto.getUserId()).isEqualTo("user");
        assertThat(dto.getComment()).isEqualTo("Post comment");
        assertThat(dto.getCreatedDate()).isEqualTo(expectedTime);
    }
}