package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostControllerIT extends AbstractControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ParameterizedTypeReference<PagedResult<PostDto>> pagedResultTypeRef =
            new ParameterizedTypeReference<PagedResult<PostDto>>() {
            };

    private ResponseEntity<PostDto> getPostByLocation(URI location) {
        return restTemplate.exchange(location, GET, null, PostDto.class);
    }

    private ResponseEntity<PagedResult<PostDto>> getAllPosts(int page, int size) {
        String url = "/posts?page={page}&size={size}";
        return restTemplate.exchange(url, GET, null, pagedResultTypeRef, page, size);
    }

    private ResponseEntity<PostDto> getPostById(String postId) {
        return restTemplate.exchange("/posts/{id}", GET, null, PostDto.class, postId);
    }

    private ResponseEntity<PagedResult<PostDto>> getAllPostsByUserId(String userId, int page, int size) {
        String url = "/posts/users/{id}?page={page}&size={size}";
        return restTemplate.exchange(url, GET, null, pagedResultTypeRef, userId, page, size);
    }

    private ResponseEntity<Object> deletePost(String postId) {
        return restTemplate.exchange("/posts/{id}", DELETE, null, Object.class, postId);
    }

    @Test
    void givenCreatePostRequest_whenPostAndFindByLocation_thenCreatedAndFound() {
        //given
        CreatePostRequest request = new CreatePostRequest("Post content");

        //when
        ResponseEntity<PostDto> createResponse = createPost(restTemplate, request);
        URI location = createResponse.getHeaders().getLocation();
        ResponseEntity<PostDto> getResponse = getPostByLocation(location);

        //then
        assertThat(createResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(getResponse.getStatusCode()).isEqualTo(OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody()).hasNoNullFieldsOrProperties();
        assertThat(getResponse.getBody().getContent()).isEqualTo("Post content");
    }

    @Test
    void givenCreatePostRequest_whenPostAndFindById_thenCreatedAndFound() {
        //given
        CreatePostRequest request = new CreatePostRequest("Post content");

        //when
        ResponseEntity<PostDto> createResponse = createPost(restTemplate, request);
        String postId = createResponse.getBody().getId();
        ResponseEntity<PostDto> getResponse = getPostById(postId);

        //then
        assertThat(getResponse.getStatusCode()).isEqualTo(OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody()).hasNoNullFieldsOrProperties();
        assertThat(getResponse.getBody().getContent()).isEqualTo("Post content");
    }

    @Test
    void givenCreatePostRequest_whenPostAndDeleteById_thenCreatedDeleted() {
        //given
        CreatePostRequest request = new CreatePostRequest("Post content");

        //when
        ResponseEntity<PostDto> createResponse = createPost(restTemplate, request);
        String postId = createResponse.getBody().getId();
        ResponseEntity<Object> deleteResponse = deletePost(postId);
        ResponseEntity<PostDto> getResponse = getPostById(postId);

        //then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(getResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void givenCreatedPosts_whenFindAll_thenFound() {
        //given
        for (int i = 0; i < 10; i++) {
            CreatePostRequest request = new CreatePostRequest(RandomStringUtils.randomAlphabetic(15));
            createPost(restTemplate, request);
        }

        //when
        ResponseEntity<PagedResult<PostDto>> allPostsResponse = getAllPosts(2, 4);

        //then
        assertThat(allPostsResponse.getStatusCode()).isEqualTo(OK);
        assertThat(allPostsResponse.getBody()).isNotNull();
        assertThat(allPostsResponse.getBody().getTotalResults()).isEqualTo(10L);
        assertThat(allPostsResponse.getBody().getPage()).isEqualTo(2);
        assertThat(allPostsResponse.getBody().getTotalPages()).isEqualTo(3);
        assertThat(allPostsResponse.getBody().getResult().size()).isEqualTo(2);
    }

    @Test
    void givenNoPost_whenGetById_thenNotFound() {
        //given
        String postId = UUID.randomUUID().toString();

        //when
        ResponseEntity<PostDto> getResponse = getPostById(postId);

        //then
        assertThat(getResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    @Disabled
    void givenUserPosts_whenGetPostsByUserId_thenNotFound() {

    }

    @Test
    void givenNoPost_whenDeleteById_thenNotFound() {
        //given
        String postId = UUID.randomUUID().toString();

        //when
        ResponseEntity<Object> deleteResponse = deletePost(postId);

        //then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }
}