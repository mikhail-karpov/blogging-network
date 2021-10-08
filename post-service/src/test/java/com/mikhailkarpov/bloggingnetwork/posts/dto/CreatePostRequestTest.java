package com.mikhailkarpov.bloggingnetwork.posts.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreatePostRequestTest {

    @Autowired
    private JacksonTester<CreatePostRequest> requestJacksonTester;

    @Test
    void testSerialize() throws IOException {
        //given
        CreatePostRequest request = new CreatePostRequest("post content");

        //when
        JsonContent<CreatePostRequest> json = requestJacksonTester.write(request);

        //then
        assertThat(json).extractingJsonPathStringValue("$.content").isEqualTo("post content");
    }

    @Test
    void testDeserialize() throws IOException {
        //given
        String json = "{\"content\": \"post content\"}";

        //when
        CreatePostRequest request = requestJacksonTester.parse(json).getObject();

        //then
        assertThat(request).isNotNull();
        assertThat(request.getContent()).isEqualTo("post content");
    }
}