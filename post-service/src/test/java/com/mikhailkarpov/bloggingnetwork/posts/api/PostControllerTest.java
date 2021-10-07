package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.DtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.PostDtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = PostController.class)
@Import(PostControllerTest.PostControllerTestConfig.class)
@AutoConfigureJsonTesters
class PostControllerTest {

    @TestConfiguration
    static class PostControllerTestConfig {

        @Bean
        public PostDtoMapper postDtoMapper() {
            return new PostDtoMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private DtoMapper<Post, PostDto> postDtoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonTester<PostDto> dtoJacksonTester;

    @Autowired
    private JacksonTester<PagedResult<PostDto>> pagedDtoJacksonTester;

    private final CreatePostRequest request = new CreatePostRequest(RandomStringUtils.randomAlphabetic(10));
    private final Post post = new Post(UUID.randomUUID().toString(), request.getContent());
    private final PostDto postDto = PostDto.builder()
            .id(post.getId().toString())
            .userId(post.getUserId())
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
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost/posts/" + postDto.getId());
        assertThat(response.getContentAsString()).isEqualTo(dtoJacksonTester.write(postDto).getJson());
        verify(postService).save(any());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void givenNotValidRequest_whenCreatePost_thenBadRequest(CreatePostRequest request) throws Exception {
        //when
        MockHttpServletResponse response = mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(400);
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
        MockHttpServletResponse response = mockMvc.perform(get("/posts?page=1&size=5"))
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
        MockHttpServletResponse response = mockMvc.perform(get("/posts/{id}", postId))
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
        MockHttpServletResponse response = mockMvc.perform(get("/posts/{id}", postId))
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
        MockHttpServletResponse response = mockMvc.perform(get("/posts/users/{id}?page=1&size=5", userId))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedDtoJacksonTester.write(pagedResult).getJson());
        verify(postService).findAllByUserId(userId, pageRequest);
    }

    @Test
    void testDeletePostById() throws Exception {
        //given
        String postId = UUID.randomUUID().toString();

        //when
        MockHttpServletResponse response = mockMvc.perform(delete("/posts/{id}", postId))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getContentAsString()).isEmpty();
        verify(postService).deleteById(UUID.fromString(postId));
    }
}