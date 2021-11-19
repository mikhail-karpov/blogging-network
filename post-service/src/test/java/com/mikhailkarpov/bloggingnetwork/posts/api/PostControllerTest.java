package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest extends AbstractControllerTest {

    @MockBean
    PostService postService;

    @Captor
    ArgumentCaptor<Post> postArgumentCaptor;

    @Test
    void givenRequest_whenCreatePost_thenCreated() throws Exception {
        //given
        String userId = "user-id";
        Post post = new Post(userId, "post content");

        when(postService.save(any())).thenReturn(post);

        //when
        mockMvc.perform(post("/posts")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePostRequest("post content"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/posts/" + post.getId()));

        //then
        verify(postService).save(postArgumentCaptor.capture());
        Assertions.assertThat(postArgumentCaptor.getValue().getUserId()).isEqualTo(userId);
        Assertions.assertThat(postArgumentCaptor.getValue().getContent()).isEqualTo("post content");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void givenNotValidRequest_whenCreatePost_thenBadRequest(CreatePostRequest request) throws Exception {
        //when
        mockMvc.perform(post("/posts")
                        .with(jwt())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(postService);
    }

    private static Stream<Arguments> getInvalidRequests() {
        return Stream.of(
                Arguments.of(new CreatePostRequest(null)),
                Arguments.of(new CreatePostRequest("")),
                Arguments.of(RandomStringUtils.randomAlphabetic(3)),
                Arguments.of(RandomStringUtils.randomAlphabetic(181))
        );
    }

    @Test
    void givenPost_whenFindById_thenFound() throws Exception {
        //given
        String userId = "user-id";
        Post post = new Post(userId, "post content");

        when(postService.findById(post.getId(), false)).thenReturn(Optional.of(post));
        when(userService.getUserById(userId)).thenReturn(new UserProfileDto(userId, "username"));

        //when
        mockMvc.perform(get("/posts/{id}", post.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.content").value("post content"))
                .andExpect(jsonPath("$.user.userId").value("user-id"))
                .andExpect(jsonPath("$.user.username").value("username"));

        //then
        verify(postService).findById(post.getId(), false);
        verify(userService).getUserById(userId);
    }

    @Test
    void givenNoPost_whenFindById_thenNotFound() throws Exception {
        //given
        when(postService.findById(any())).thenReturn(Optional.empty());

        //when
        mockMvc.perform(get("/posts/{id}", UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isNotFound());

        //then
        verify(postService).findById(any(), eq(false));
    }

    @Test
    void givenPosts_whenFindByUserId_thenFound() throws Exception {
        //given
        String userId = "user-id";
        PageRequest pageRequest = PageRequest.of(1, 5);
        Post post = new Post(userId, "post content");
        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post), pageRequest, 6L);

        when(postService.findAllByUserId(userId, pageRequest)).thenReturn(postPage);
        when(userService.getUserById(userId)).thenReturn(new UserProfileDto(userId, "username"));

        //when
        String url = "/posts/users/{id}?page=1&size=5";
        mockMvc.perform(get(url, userId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(6))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.size()").value(1))
                .andExpect(jsonPath("$.result[0].id").value(post.getId().toString()))
                .andExpect(jsonPath("$.result[0].content").value("post content"))
                .andExpect(jsonPath("$.result[0].user.userId").value("user-id"))
                .andExpect(jsonPath("$.result[0].user.username").value("username"));

        //then
        verify(postService).findAllByUserId(userId, pageRequest);
        verify(userService).getUserById(userId);
    }

    @Test
    void givenPost_whenDeletePostById_thenNoContent() throws Exception {
        //given
        Post post = new Post("user-id", "content");
        when(postService.findById(post.getId(), false)).thenReturn(Optional.of(post));

        //when
        mockMvc.perform(delete("/posts/{id}", post.getId())
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isNoContent());

        //then
        verify(postService).findById(post.getId(), false);
        verify(postService).deleteById(post.getId());
    }

    @Test
    void givenNoPost_whenDeletePostById_thenNotFound() throws Exception {
        //given
        when(postService.findById(any(), eq(false))).thenReturn(Optional.empty());

        //when
        mockMvc.perform(delete("/posts/{id}", UUID.randomUUID())
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(postService).findById(any(), eq(false));
        verifyNoMoreInteractions(postService);
    }
}