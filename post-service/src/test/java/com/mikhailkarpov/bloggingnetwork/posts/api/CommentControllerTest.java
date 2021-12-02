package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreateCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import com.mikhailkarpov.bloggingnetwork.posts.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(controllers = CommentController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserService userService;

    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @Test
    void givenRequest_whenPostComment_thenCreated() throws Exception {
        //given
        String userId = "user-id";
        UUID postId = UUID.randomUUID();
        Comment comment = new Comment(userId, "post comment");
        String expectedLocation = "http://localhost/posts/" + postId + "/comments/" + comment.getId();

        when(commentService.createComment(postId, userId, "post comment")).thenReturn(comment.getId());

        //when
        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(APPLICATION_JSON)
                        .content("{\"comment\":\"post comment\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", expectedLocation));

        //then
        verify(commentService).createComment(postId, userId, "post comment");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidCommentRequest")
    void givenInvalidRequest_whenPostComment_thenBadRequest(CreateCommentRequest request) throws Exception {
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
                Arguments.of(new CreateCommentRequest(null)),
                Arguments.of(new CreateCommentRequest("")),
                Arguments.of(new CreateCommentRequest(RandomStringUtils.randomAlphabetic(3))),
                Arguments.of(new CreateCommentRequest(RandomStringUtils.randomAlphabetic(181)))
        );
    }

    @Test
    void givenComments_whenGetCommentsByPostId_thenOk() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(1, 2);
        List<Comment> comments = Arrays.asList(
                new Comment("user1", "comment1"),
                new Comment("user2", "comment2")
        );
        Page<Comment> postCommentPage =
                new PageImpl<>(comments, pageRequest, 10L);

        when(commentService.findAllByPostId(postId, pageRequest)).thenReturn(postCommentPage);
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
                .andExpect(jsonPath("$.result[0].user.userId").value("user1"))
                .andExpect(jsonPath("$.result[0].user.username").value("username1"))
                .andExpect(jsonPath("$.result[1].id").isNotEmpty())
                .andExpect(jsonPath("$.result[1].comment").value("comment2"))
                .andExpect(jsonPath("$.result[1].user.userId").value("user2"))
                .andExpect(jsonPath("$.result[1].user.username").value("username2"));

        //then
        verify(commentService).findAllByPostId(postId, pageRequest);
        verify(userService).getUserById("user1");
        verify(userService).getUserById("user2");
    }

    @Test
    void givenComment_whenGetCommentById_thenOk() throws Exception {
        //given
        Comment comment = new Comment("user1", "comment");
        UserProfileDto user = new UserProfileDto("user1", "username");

        when(commentService.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userService.getUserById(user.getUserId())).thenReturn(user);

        //when
        mockMvc.perform(get("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), comment.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId().toString()))
                .andExpect(jsonPath("$.comment").value(comment.getComment()))
                .andExpect(jsonPath("$.user.userId").value("user1"))
                .andExpect(jsonPath("$.user.username").value("username"));

        //then
        verify(commentService).findById(comment.getId());
    }

    @Test
    void givenNoComment_whenGetCommentById_thenNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(commentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(get("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId)
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(commentService).findById(commentId);
    }

    @Test
    void givenComment_whenDelete_thenNoContent() throws Exception {
        //given
        Comment comment = new Comment("user-id", "comment");
        when(commentService.findById(comment.getId())).thenReturn(Optional.of(comment));

        //when
        UUID postId = UUID.randomUUID();
        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", postId, comment.getId())
                        .with(jwt().jwt(jwt -> jwt.subject("user-id"))))
                .andExpect(status().isNoContent());

        //then
        verify(commentService).findById(comment.getId());
        verify(commentService).removeComment(postId, comment.getId());
    }

    @Test
    void givenNoComment_whenDelete_thenNoContent() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        when(commentService.findById(commentId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId)
                        .with(jwt()))
                .andExpect(status().isNotFound());

        //then
        verify(commentService).findById(commentId);
        verifyNoMoreInteractions(commentService);
    }
}