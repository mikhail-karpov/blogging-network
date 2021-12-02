package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreateCommentRequestTest {

    @Autowired
    private JacksonTester<CreateCommentRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        CreateCommentRequest request = new CreateCommentRequest("Post comment");

        //when
        JsonContent<CreateCommentRequest> json = jacksonTester.write(request);

        //then
        assertThat(json).extractingJsonPathStringValue("$.comment").isEqualTo("Post comment");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{\"comment\": \"Post comment\"}";

        //when
        CreateCommentRequest request = jacksonTester.parse(json).getObject();

        //then
        assertThat(request.getComment()).isEqualTo("Post comment");
    }
}