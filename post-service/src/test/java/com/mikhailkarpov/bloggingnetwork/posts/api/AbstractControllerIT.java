package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpMethod.POST;

public abstract class AbstractControllerIT extends AbstractIT {

    public ResponseEntity<PostDto> createPost(TestRestTemplate restTemplate, CreatePostRequest request) {
        HttpEntity<CreatePostRequest> requestEntity = new HttpEntity<>(request, new HttpHeaders());
        return restTemplate.exchange("/posts", POST, requestEntity, PostDto.class);
    }
}
