package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.CommentDtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.DtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
import org.junit.jupiter.api.Disabled;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(controllers = PostCommentController.class)
@Import(PostCommentControllerTest.PostCommentControllerTestConfig.class)
@AutoConfigureJsonTesters
class PostCommentControllerTest {

    @TestConfiguration
    static class PostCommentControllerTestConfig {

        @Bean
        public DtoMapper<PostComment, PostCommentDto> commentDtoMapper() {
            return new CommentDtoMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostCommentService postCommentService;

    @Autowired
    private DtoMapper<PostComment, PostCommentDto> commentDtoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonTester<PostCommentDto> dtoTester;

    @Autowired
    private JacksonTester<PagedResult<PostCommentDto>> pagedResultTester;

    private final CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");
    private final UUID postId = UUID.randomUUID();
    private final String userId = UUID.randomUUID().toString();
    private final PostComment postComment = new PostComment(userId, "Post comment");
    private final PostCommentDto postCommentDto =
            new PostCommentDto(postComment.getId().toString(), userId, "Post comment", postComment.getCreatedDate());

    @Test
    void givenCreatePostCommentRequest_whenPostComment_thenCreated() throws Exception {
        //given
        String expectedLocation = "http://localhost/posts/" + postId + "/comments/" + postComment.getId().toString();
        when(postCommentService.addComment(any(), any())).thenReturn(postComment);

        //when
        String uri = "/posts/{id}/comments";
        MockHttpServletResponse response = mockMvc.perform(post(uri, postId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader("Location")).isEqualTo(expectedLocation);
        assertThat(response.getContentAsString()).isEqualTo(dtoTester.write(postCommentDto).getJson());
        verify(postCommentService).addComment(any(), any());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidCommentRequest")
    void givenInvalidRequest_whenPostComment_thenBadRequest(CreatePostCommentRequest request) throws Exception {
        //when
        String uri = "/posts/{id}/comments";
        MockHttpServletResponse response = mockMvc.perform(post(uri, postId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(400);
    }

    private static Stream<Arguments> getInvalidCommentRequest() {
        return Stream.of(
                Arguments.of(new CreatePostCommentRequest(null)),
                Arguments.of(new CreatePostCommentRequest("")),
                Arguments.of(new CreatePostCommentRequest(RandomStringUtils.randomAlphabetic(3))),
                Arguments.of(new CreatePostCommentRequest(RandomStringUtils.randomAlphabetic(181)))
        );
    }

    @Test
    void givenNotValidPostId_whenAddComment_thenNotFound() throws Exception {
        //given
        String postId = "abc";

        //when
        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenComments_whenGetCommentsByPostId_thenOk() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(1, 3);
        Page<PostComment> postCommentPage =
                new PageImpl<>(Arrays.asList(postComment), pageRequest, 10L);
        PagedResult<PostCommentDto> expectedPagedResult =
                new PagedResult<>(new PageImpl<>(Arrays.asList(postCommentDto), pageRequest, 10L));
        when(postCommentService.findAllByPostId(postId, pageRequest)).thenReturn(postCommentPage);

        //when
        String uri = "/posts/{postId}/comments?page={page}&size={size}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, 1, 3))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedResultTester.write(expectedPagedResult).getJson());
        verify(postCommentService).findAllByPostId(postId, pageRequest);
    }

    @Test
    void givenNotValidPostId_whenGetComments_thenNotFound() throws Exception {
        //given
        String postId = "abc";

        //when
        mockMvc.perform(get("/posts/{id}/comments", postId))
                .andExpect(status().isNotFound());

        //then
        verifyNoInteractions(postCommentService);
    }

    @Test
    void givenComment_whenGetCommentById_thenOk() throws Exception {
        //given
        UUID commentId = postComment.getId();
        when(postCommentService.findById(commentId)).thenReturn(Optional.of(postComment));

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, commentId))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(dtoTester.write(postCommentDto).getJson());
        verify(postCommentService).findById(commentId);
    }

    @Test
    void givenNotValidCommentId_whenGetCommentById_thenNotFound() throws Exception {
        //given
        String commentId = "abc";

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        mockMvc.perform(get(uri, postId, commentId))
                .andExpect(status().isNotFound());

        //then
        verifyNoInteractions(postCommentService);
    }

    @Test
    void givenNoComment_whenGetCommentById_thenNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(postCommentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, commentId))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(404);
        verify(postCommentService).findById(commentId);
    }

    @Test
    void whenDeleteComment_thenNoContent() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(delete(uri, postId, commentId))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(204);
        verify(postCommentService).removeComment(postId, commentId);
    }

    @Test
    void givenNotValidPostId_whenDeleteComment_thenNotFound() throws Exception {
        //given
        String postId = "abc";
        String commentId = "def";

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        mockMvc.perform(delete(uri, postId, commentId))
                .andExpect(status().isNotFound());

        //then
        verifyNoInteractions(postCommentService);
    }

    @Test
    void givenNotValidCommentId_whenDeleteComment_thenNotFound() throws Exception {
        //given
        String commentId = "def";

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        mockMvc.perform(delete(uri, postId, commentId))
                .andExpect(status().isNotFound());

        //then
        verifyNoInteractions(postCommentService);
    }
}