package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreatePostCommentRequestTest {

    @Autowired
    private JacksonTester<CreatePostCommentRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");

        //when
        JsonContent<CreatePostCommentRequest> json = jacksonTester.write(request);

        //then
        assertThat(json).extractingJsonPathStringValue("$.comment").isEqualTo("Post comment");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{\"comment\": \"Post comment\"}";

        //when
        CreatePostCommentRequest request = jacksonTester.parse(json).getObject();

        //then
        assertThat(request.getComment()).isEqualTo("Post comment");
    }
}