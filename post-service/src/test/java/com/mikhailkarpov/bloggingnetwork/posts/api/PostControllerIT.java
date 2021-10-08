package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostControllerIT extends AbstractControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    private String accessToken;

    @BeforeEach
    void obtainAccessToken() {
        accessToken = oAuth2Client.obtainAccessToken(oAuth2User);
    }

    private ResponseEntity<PostDto> getPostByLocation(URI location) {

        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        return restTemplate.exchange(location, GET, new HttpEntity<>(headers), PostDto.class);
    }

    private ResponseEntity<PagedResult<PostDto>> getAllPosts(int page, int size) {
        String url = "/posts?page={page}&size={size}";
        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        ParameterizedTypeReference<PagedResult<PostDto>> typeReference =
                new ParameterizedTypeReference<PagedResult<PostDto>>() {
                };
        return restTemplate.exchange(url, GET, new HttpEntity<>(headers), typeReference, page, size);
    }

    private ResponseEntity<PostDto> getPostById(String postId) {
        String url = "/posts/{id}";
        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        return restTemplate.exchange(url, GET, new HttpEntity<>(headers), PostDto.class, postId);
    }

    private ResponseEntity<PagedResult<PostDto>> getAllPostsByUserId(String userId, int page, int size) {
        String url = "/posts/users/{id}?page={page}&size={size}";
        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        ParameterizedTypeReference<PagedResult<PostDto>> typeReference =
                new ParameterizedTypeReference<PagedResult<PostDto>>() {
                };
        return restTemplate.exchange(url, GET, new HttpEntity<>(headers), typeReference, userId, page, size);
    }

    private ResponseEntity<Object> deletePost(String postId) {
        String url = "/posts/{id}";
        HttpHeaders headers = buildAuthorizationHeaders(accessToken);
        return restTemplate.exchange(url, DELETE, new HttpEntity<>(headers), Object.class, postId);
    }

    @Test
    void givenCreatePostRequest_whenPostAndFindByLocation_thenCreatedAndFound() {
        //given
        CreatePostRequest request = new CreatePostRequest("Post content");

        //when
        ResponseEntity<PostDto> createResponse = createPost(accessToken, restTemplate, request);
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
        ResponseEntity<PostDto> createResponse = createPost(accessToken, restTemplate, request);
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
        ResponseEntity<PostDto> createResponse = createPost(accessToken, restTemplate, request);
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
            createPost(accessToken, restTemplate, request);
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
    void givenUserPosts_whenGetPostsByUserId_thenNotFound() {
        //given
        String userId = null;
        for (int i = 0; i < 10; i++) {
            CreatePostRequest request = new CreatePostRequest(RandomStringUtils.randomAlphabetic(100));
            userId = createPost(accessToken, restTemplate, request).getBody().getUserId();
        }

        //when
        ResponseEntity<PagedResult<PostDto>> posts = getAllPostsByUserId(userId, 2, 4);

        //then
        assertThat(posts.getStatusCode()).isEqualTo(OK);
        assertThat(posts.getBody()).isNotNull();
        assertThat(posts.getBody().getTotalResults()).isEqualTo(10L);
        assertThat(posts.getBody().getPage()).isEqualTo(2);
        assertThat(posts.getBody().getTotalPages()).isEqualTo(3);
        assertThat(posts.getBody().getResult().size()).isEqualTo(2);

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