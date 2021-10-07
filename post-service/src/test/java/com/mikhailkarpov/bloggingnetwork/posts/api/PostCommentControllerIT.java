package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostCommentControllerIT extends AbstractControllerIT {

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String postId;

    @BeforeEach
    void createPost() {
        CreatePostRequest request = new CreatePostRequest("Post content");
        ResponseEntity<PostDto> createPostResponse = createPost(restTemplate, request);
        postId = createPostResponse.getBody().getId();
    }

    private ResponseEntity<PostCommentDto> addComment(String postId, CreatePostCommentRequest request) {
        HttpEntity<CreatePostCommentRequest> requestEntity = new HttpEntity<>(request, new HttpHeaders());
        String url = "/posts/{id}/comments";
        return restTemplate.exchange(url, POST, requestEntity, PostCommentDto.class, postId);
    }

    private ResponseEntity<Object> removeComment(String postId, String commentId) {
        String url = "/posts/{postId}/comments/{commentId}";
        return restTemplate.exchange(url, DELETE, null, Object.class, postId, commentId);
    }

    private ResponseEntity<PostCommentDto> getCommentById(String postId, String commentId) {
        String url = "/posts/{postId}/comments/{commentId}";
        return restTemplate.exchange(url, GET, null, PostCommentDto.class, postId, commentId);
    }

    private ResponseEntity<PagedResult<PostCommentDto>> getCommentsByPostId(String postId, int page, int size) {
        ParameterizedTypeReference<PagedResult<PostCommentDto>> typeRef =
                new ParameterizedTypeReference<PagedResult<PostCommentDto>>() {
                };
        String url = "/posts/{id}/comments?page={page}&size={size}";
        return restTemplate.exchange(url, GET, null, typeRef, postId, page, size);
    }

    @Test
    void givenCreatePostCommentRequest_whenPostComment_thenCreated() {
        //given
        CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");

        //when
        ResponseEntity<PostCommentDto> postResponse = addComment(postId, request);

        //then
        assertThat(postResponse.getStatusCode()).isEqualTo(CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody()).hasNoNullFieldsOrProperties();
        assertThat(postResponse.getBody().getComment()).isEqualTo("Post comment");
        assertThat(postResponse.getHeaders().getLocation()).isNotNull();
        assertThat(postResponse.getHeaders().getLocation().toString()).isEqualTo(
                String.format("http://localhost:%d/posts/%s/comments/%s", port, postId, postResponse.getBody().getId())
        );
    }

    @Test
    void givenCommentPosted_whenGetById_thenFound() {
        //given
        CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");
        String commentId = addComment(postId, request).getBody().getId();

        //when
        ResponseEntity<PostCommentDto> getResponse = getCommentById(postId, commentId);

        //then
        assertThat(getResponse.getStatusCode()).isEqualTo(OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody()).hasNoNullFieldsOrProperties();
        assertThat(getResponse.getBody().getComment()).isEqualTo("Post comment");
    }

    @Test
    void givenCommentPosted_whenDeleteById_thenDeleted() {
        //given
        CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");
        String commentId = addComment(postId, request).getBody().getId();

        //when
        ResponseEntity<Object> deleteResponse = removeComment(postId, commentId);
        ResponseEntity<PostCommentDto> getResponse = getCommentById(postId, commentId);

        //then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(getResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void givenNoPost_whenGetCommentById_thenNotFound() {
        //given
        String commentId = UUID.randomUUID().toString();

        //when
        ResponseEntity<PostCommentDto> comment = getCommentById(postId, commentId);

        //then
        assertThat(comment.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void givenPostComments_whenGetCommentsByPostId_thenFound() {
        //given
        for (int i = 0; i < 10; i++) {
            CreatePostCommentRequest request = new CreatePostCommentRequest(RandomStringUtils.randomAlphabetic(14));
            addComment(postId, request);
        }

        //when
        ResponseEntity<PagedResult<PostCommentDto>> comments = getCommentsByPostId(postId, 2, 4);

        //then
        assertThat(comments.getStatusCode()).isEqualTo(OK);
        assertThat(comments.getBody()).isNotNull();
        assertThat(comments.getBody()).hasNoNullFieldsOrProperties();
        assertThat(comments.getBody().getPage()).isEqualTo(2);
        assertThat(comments.getBody().getTotalPages()).isEqualTo(3);
        assertThat(comments.getBody().getTotalResults()).isEqualTo(10L);
        assertThat(comments.getBody().getResult().size()).isEqualTo(2);
    }

    @Test
    void givenNoPost_whenDeleteComment_thenNotFound() {
        //given
        String postId = UUID.randomUUID().toString();
        String commentId = UUID.randomUUID().toString();

        //when
        ResponseEntity<Object> deleteResponse = removeComment(postId, commentId);

        //then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void givenNoComment_whenDeleteComment_thenNotFound() {
        //given
        String commentId = UUID.randomUUID().toString();

        //when
        ResponseEntity<Object> deleteResponse = removeComment(postId, commentId);

        //then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }
}