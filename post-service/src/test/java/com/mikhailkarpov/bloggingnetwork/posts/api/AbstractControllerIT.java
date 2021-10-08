package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.util.OAuth2Client;
import com.mikhailkarpov.bloggingnetwork.posts.util.OAuth2User;
import com.mikhailkarpov.bloggingnetwork.posts.config.IntegrationTestSecurityConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.http.HttpMethod.POST;

@ContextConfiguration(classes = {IntegrationTestSecurityConfig.class})
public abstract class AbstractControllerIT extends AbstractIT {

    @Autowired
    OAuth2Client oAuth2Client;

    @Autowired
    OAuth2User oAuth2User;

    public ResponseEntity<PostDto> createPost(String accessToken, TestRestTemplate restTemplate, CreatePostRequest request) {

        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        HttpEntity<CreatePostRequest> requestEntity = new HttpEntity<>(request, headers);

        return restTemplate.exchange("/posts", POST, requestEntity, PostDto.class);
    }

    protected HttpHeaders buildAuthorizationHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        return headers;
    }
}
