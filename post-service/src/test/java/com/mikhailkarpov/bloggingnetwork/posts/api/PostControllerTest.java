package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureJsonTesters
@ContextConfiguration(classes = SecurityTestConfig.class)
class PostControllerTest {

    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonTester<PostDto> postTester;

    @Autowired
    private JacksonTester<PagedResult<PostDto>> pagedResultTester;

    private final UserProfileDto user = new UserProfileDto("user-id", "user-name");

    private final List<PostDto> postList = Arrays.asList(
            PostDto.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .content(RandomStringUtils.randomAlphabetic(10))
                    .createdDate(Instant.now().minus(1L, ChronoUnit.DAYS))
                    .build(),

            PostDto.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .content(RandomStringUtils.randomAlphabetic(10))
                    .createdDate(Instant.now().minus(2L, ChronoUnit.DAYS))
                    .build()
    );

    @Test
    void givenRequest_whenCreatePost_thenCreated() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        this.mockMvc.perform(post("/posts")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePostRequest("post content"))))
                .andExpect(status().isCreated());

        //then
        verify(this.postService).createPost(userId, "post content");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void givenNotValidRequest_whenCreatePost_thenBadRequest(CreatePostRequest request) throws Exception {
        //when
        this.mockMvc.perform(post("/posts")
                        .with(jwt())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(this.postService);
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
        PostDto post = this.postList.get(0);
        UUID postId = UUID.fromString(post.getId());
        when(this.postService.findById(postId)).thenReturn(Optional.of(post));

        //when
        MockHttpServletResponse response = mockMvc.perform(get("/posts/{id}", postId)
                        .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(this.postTester.write(post).getJson());
        verify(this.postService).findById(postId);
    }

    @Test
    void givenNoPost_whenFindById_thenNotFound() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        when(this.postService.findById(postId)).thenReturn(Optional.empty());

        //when
        this.mockMvc.perform(get("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isNotFound());

        //then
        verify(this.postService).findById(postId);
    }

    @Test
    void givenPosts_whenFindByUserId_thenFound() throws Exception {
        //given
        String userId = this.user.getUserId();
        PageRequest pageRequest = PageRequest.of(1, 5);
        Page<PostDto> postPage = new PageImpl<>(this.postList, pageRequest, 6L);

        when(this.postService.findAllByUserId(userId, pageRequest)).thenReturn(postPage);

        //when
        String url = "/posts/users/{id}?page=1&size=5";
        MockHttpServletResponse response = this.mockMvc.perform(get(url, userId)
                        .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
                .isEqualTo(this.pagedResultTester.write(new PagedResult<>(postPage)).getJson());

        verify(this.postService).findAllByUserId(userId, pageRequest);
    }

    @Test
    void givenPost_whenDeletePostById_thenNoContent() throws Exception {
        //given
        PostDto post = this.postList.get(0);
        UUID postId = UUID.fromString(post.getId());

        when(this.postService.findById(postId)).thenReturn(Optional.of(post));

        //when
        String userId = post.getUser().getUserId();
        this.mockMvc.perform(delete("/posts/{id}", post.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());

        //then
        verify(this.postService).findById(postId);
        verify(this.postService).deleteById(postId);
    }

    @Test
    void givenNoPost_whenDeletePostById_thenNotFound() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        when(this.postService.findById(postId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(delete("/posts/{id}", postId)
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(postService).findById(postId);
        verifyNoMoreInteractions(postService);
    }
}