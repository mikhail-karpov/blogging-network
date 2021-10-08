package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.config.DtoMapperConfig;
import com.mikhailkarpov.bloggingnetwork.posts.config.TestSecurityConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(controllers = PostCommentController.class)
@ContextConfiguration(classes = {DtoMapperConfig.class, TestSecurityConfig.class})
@AutoConfigureJsonTesters
class PostCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostCommentService postCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JacksonTester<PostCommentDto> dtoTester;

    @Autowired
    private JacksonTester<PagedResult<PostCommentDto>> pagedResultTester;

    @Captor
    private ArgumentCaptor<PostComment> commentArgumentCaptor;

    private final CreatePostCommentRequest request = new CreatePostCommentRequest("Post comment");
    private final UUID postId = UUID.randomUUID();
    private final String userId = TestSecurityConfig.SUBJECT;
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
                        .header("Authorization", "Bearer token")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader("Location")).isEqualTo(expectedLocation);
        assertThat(response.getContentAsString()).isEqualTo(dtoTester.write(postCommentDto).getJson());

        verify(postCommentService).addComment(eq(postId), commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(commentArgumentCaptor.getValue().getContent()).isEqualTo(request.getComment());
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidCommentRequest")
    void givenInvalidRequest_whenPostComment_thenBadRequest(CreatePostCommentRequest request) throws Exception {
        //when
        String uri = "/posts/{id}/comments";
        MockHttpServletResponse response = mockMvc.perform(post(uri, postId)
                        .header("Authorization", "Bearer token")
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
    void givenComments_whenGetCommentsByPostId_thenOk() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(1, 3);
        Page<PostComment> postCommentPage =
                new PageImpl<>(Collections.singletonList(postComment), pageRequest, 10L);
        PagedResult<PostCommentDto> expectedPagedResult =
                new PagedResult<>(new PageImpl<>(Collections.singletonList(postCommentDto), pageRequest, 10L));
        when(postCommentService.findAllByPostId(postId, pageRequest)).thenReturn(postCommentPage);

        //when
        String uri = "/posts/{postId}/comments?page={page}&size={size}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, 1, 3)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedResultTester.write(expectedPagedResult).getJson());
        verify(postCommentService).findAllByPostId(postId, pageRequest);
    }

    @Test
    void givenComment_whenGetCommentById_thenOk() throws Exception {
        //given
        UUID commentId = postComment.getId();
        when(postCommentService.findById(commentId)).thenReturn(Optional.of(postComment));

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, commentId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(dtoTester.write(postCommentDto).getJson());
        verify(postCommentService).findById(commentId);
    }

    @Test
    void givenNoComment_whenGetCommentById_thenNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(postCommentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(get(uri, postId, commentId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(404);
        verify(postCommentService).findById(commentId);
    }

    @Test
    void givenComment_whenDelete_thenNoContent() throws Exception {
        //given
        UUID commentId = postComment.getId();
        when(postCommentService.findById(commentId)).thenReturn(Optional.of(postComment));

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(delete(uri, postId, commentId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(204);
        verify(postCommentService).findById(commentId);
        verify(postCommentService).removeComment(postId, commentId);
    }

    @Test
    void givenNoComment_whenDelete_thenNoContent() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(postCommentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        String uri = "/posts/{postId}/comments/{commentId}";
        MockHttpServletResponse response = mockMvc.perform(delete(uri, postId, commentId)
                        .header("Authorization", "Bearer token"))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(404);
        verify(postCommentService).findById(commentId);
        verifyNoMoreInteractions(postCommentService);
    }
}