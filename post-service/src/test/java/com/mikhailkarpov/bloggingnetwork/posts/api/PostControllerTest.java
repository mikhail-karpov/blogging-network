package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.TestSecurityConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(PostController.class)
@AutoConfigureJsonTesters
class PostControllerTest extends AbstractControllerTest {

    @MockBean
    PostService postService;

    @Autowired
    private JacksonTester<PostDto> dtoJacksonTester;

    @Autowired
    private JacksonTester<PagedResult<PostDto>> pagedDtoJacksonTester;

    @Captor
    private ArgumentCaptor<Post> postArgumentCaptor;

    private final String userId = TestSecurityConfig.SUBJECT;
    private final CreatePostRequest request = new CreatePostRequest(RandomStringUtils.randomAlphabetic(10));
    private final Post post = new Post(userId, request.getContent());
    private final PostDto postDto = PostDto.builder()
            .id(post.getId().toString())
            .userId(userId)
            .content(post.getContent())
            .createdDate(post.getCreatedDate())
            .build();

    private final PageRequest pageRequest = PageRequest.of(1, 5);
    private final Page<Post> postPage = new PageImpl<>(Collections.singletonList(post), pageRequest, 6L);
    private final PagedResult<PostDto> pagedResult =
            new PagedResult<>(new PageImpl<>(Collections.singletonList(postDto), pageRequest, 6L));

    @Test
    void givenRequest_whenCreatePost_thenCreated() throws Exception {
        //given
        when(postService.save(any())).thenReturn(post);

        //when
        MockHttpServletResponse response = mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer token")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost/posts/" + postDto.getId());
        assertThat(response.getContentAsString()).isEqualTo(dtoJacksonTester.write(postDto).getJson());

        verify(postService).save(postArgumentCaptor.capture());
        Post captorValue = postArgumentCaptor.getValue();
        assertThat(captorValue).isNotNull();
        assertThat(captorValue.getUserId()).isEqualTo(userId);
        assertThat(captorValue.getContent()).isEqualTo(request.getContent());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void givenNotValidRequest_whenCreatePost_thenBadRequest(CreatePostRequest request) throws Exception {
        //when
        MockHttpServletResponse response = mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer token")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(400);
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
    void givenPosts_whenFindAll_thenFound() throws Exception {
        //given
        when(postService.findAll(pageRequest)).thenReturn(postPage);

        //when
        MockHttpServletResponse response = mockMvc.perform(get("/posts?page=1&size=5")
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedDtoJacksonTester.write(pagedResult).getJson());
        verify(postService).findAll(pageRequest);
    }

    @Test
    void givenPost_whenFindById_thenFound() throws Exception {
        //given
        UUID postId = post.getId();
        when(postService.findById(postId, false)).thenReturn(Optional.of(post));

        //when
        MockHttpServletResponse response = mockMvc.perform(get("/posts/{id}", postId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(dtoJacksonTester.write(postDto).getJson());
        verify(postService).findById(postId, false);
    }

    @Test
    void givenNoPost_whenFindById_thenNotFound() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        when(postService.findById(postId)).thenReturn(Optional.empty());

        //when
        MockHttpServletResponse response = mockMvc.perform(get("/posts/{id}", postId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(404);
        verify(postService).findById(postId, false);
    }

    @Test
    void givenPosts_whenFindByUserId_thenFound() throws Exception {
        //given
        String userId = post.getUserId();
        when(postService.findAllByUserId(userId, pageRequest)).thenReturn(postPage);

        //when
        String url = "/posts/users/{id}?page=1&size=5";
        MockHttpServletResponse response = mockMvc.perform(get(url, userId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedDtoJacksonTester.write(pagedResult).getJson());
        verify(postService).findAllByUserId(userId, pageRequest);
    }

    @Test
    void givenPost_whenDeletePostById_thenNoContent() throws Exception {
        //given
        UUID postId = post.getId();
        when(postService.findById(postId, false)).thenReturn(Optional.of(post));

        //when
        MockHttpServletResponse response = mockMvc.perform(delete("/posts/{id}", postId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getContentAsString()).isEmpty();
        verify(postService).findById(postId, false);
        verify(postService).deleteById(postId);
    }

    @Test
    void givenNoPost_whenDeletePostById_thenNotFound() throws Exception {
        //given
        UUID postId = post.getId();
        when(postService.findById(postId, false)).thenReturn(Optional.empty());

        //when
        MockHttpServletResponse response = mockMvc.perform(delete("/posts/{id}", postId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).isEmpty();
        verify(postService).findById(postId, false);
        verifyNoMoreInteractions(postService);
    }
}