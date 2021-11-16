package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostCommentController.class)
class PostCommentControllerTest extends AbstractControllerTest {

    @MockBean
    PostCommentService postCommentService;

    @Captor
    private ArgumentCaptor<PostComment> commentArgumentCaptor;

    @Test
    void givenRequest_whenPostComment_thenCreated() throws Exception {
        //given
        String userId = "user-id";
        UUID postId = UUID.randomUUID();
        PostComment postComment = new PostComment(userId, "post comment");
        String expectedLocation = "http://localhost/posts/" + postId + "/comments/" + postComment.getId();

        when(postCommentService.addComment(any(), any())).thenReturn(postComment);

        //when
        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePostCommentRequest("comment"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation));

        //then
        verify(postCommentService).addComment(eq(postId), commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(commentArgumentCaptor.getValue().getContent()).isEqualTo("comment");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidCommentRequest")
    void givenInvalidRequest_whenPostComment_thenBadRequest(CreatePostCommentRequest request) throws Exception {
        //given
        UUID postId = UUID.randomUUID();

        //when
        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .with(jwt())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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
        UUID postId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(1, 2);
        List<PostComment> comments = Arrays.asList(
                new PostComment("user1", "comment1"),
                new PostComment("user2", "comment2")
        );
        Page<PostComment> postCommentPage =
                new PageImpl<>(comments, pageRequest, 10L);

        when(postCommentService.findAllByPostId(postId, pageRequest)).thenReturn(postCommentPage);
        when(userService.getUserById("user1")).thenReturn(new UserProfileDto("user1", "username1"));
        when(userService.getUserById("user2")).thenReturn(new UserProfileDto("user2", "username2"));

        //when
        mockMvc.perform(get("/posts/{postId}/comments?page={page}&size={size}", postId, 1, 2)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.totalResults").value(10))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.size()").value(2))
                .andExpect(jsonPath("$.result[0].id").isNotEmpty())
                .andExpect(jsonPath("$.result[0].comment").value("comment1"))
                .andExpect(jsonPath("$.result[0].createdDate").isNotEmpty())
                .andExpect(jsonPath("$.result[0].user.userId").value("user1"))
                .andExpect(jsonPath("$.result[0].user.username").value("username1"))
                .andExpect(jsonPath("$.result[1].id").isNotEmpty())
                .andExpect(jsonPath("$.result[1].comment").value("comment2"))
                .andExpect(jsonPath("$.result[1].createdDate").isNotEmpty())
                .andExpect(jsonPath("$.result[1].user.userId").value("user2"))
                .andExpect(jsonPath("$.result[1].user.username").value("username2"));

        //then
        verify(postCommentService).findAllByPostId(postId, pageRequest);
        verify(userService).getUserById("user1");
        verify(userService).getUserById("user2");
    }

    @Test
    void givenComment_whenGetCommentById_thenOk() throws Exception {
        //given
        PostComment comment = new PostComment("user1", "comment");
        UserProfileDto user = new UserProfileDto("user1", "username");

        when(postCommentService.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userService.getUserById(user.getUserId())).thenReturn(user);

        //when
        mockMvc.perform(get("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), comment.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId().toString()))
                .andExpect(jsonPath("$.comment").value(comment.getContent()))
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.user.userId").value("user1"))
                .andExpect(jsonPath("$.user.username").value("username"));

        //then
        verify(postCommentService).findById(comment.getId());
    }

    @Test
    void givenNoComment_whenGetCommentById_thenNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(postCommentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(get("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId)
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(postCommentService).findById(commentId);
    }

    @Test
    void givenComment_whenDelete_thenNoContent() throws Exception {
        //given
        PostComment comment = new PostComment("user-id", "comment");
        when(postCommentService.findById(comment.getId())).thenReturn(Optional.of(comment));

        //when
        UUID postId = UUID.randomUUID();
        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", postId, comment.getId())
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isNoContent());

        //then
        verify(postCommentService).findById(comment.getId());
        verify(postCommentService).removeComment(postId, comment.getId());
    }

    @Test
    void givenNoComment_whenDelete_thenNoContent() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(postCommentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId)
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(postCommentService).findById(commentId);
        verifyNoMoreInteractions(postCommentService);
    }
}