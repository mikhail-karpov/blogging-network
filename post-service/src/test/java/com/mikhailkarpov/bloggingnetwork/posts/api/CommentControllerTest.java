package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import org.junit.jupiter.api.Test;
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

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
@AutoConfigureJsonTesters
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private JacksonTester<CommentDto> commentTester;

    @Autowired
    private JacksonTester<PagedResult<CommentDto>> pagedResultTester;

    private final UUID commentId = UUID.randomUUID();

    private final CommentDto comment = CommentDto.builder()
            .id(commentId.toString())
            .comment("Post comment")
            .user(new UserProfileDto("user1", "username"))
            .createdDate(Instant.now())
            .build();

    @Test
    void givenRequest_whenPostComment_thenCreated() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        String subject = "user-subject";

        //when
        this.mockMvc.perform(post("/posts/{id}/comments", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(subject)))
                        .contentType(APPLICATION_JSON)
                        .content("{\"comment\":\"post comment\"}"))
                .andExpect(status().isCreated());

        //then
        verify(this.commentService).createComment(postId, subject, "post comment");
    }

    @Test
    void givenComments_whenGetCommentsByPostId_thenOk() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(1, 2);
        Page<CommentDto> postCommentPage = new PageImpl<>(Collections.singletonList(this.comment), pageRequest, 3L);

        when(this.commentService.findAllByPostId(postId, pageRequest)).thenReturn(postCommentPage);

        //when
        String url = String.format("/posts/%s/comments?page=%d&size=%d", postId, 1, 2);
        MockHttpServletResponse response = this.mockMvc.perform(get(url).with(jwt()))
                .andReturn()
                .getResponse();

        //then
        verify(commentService).findAllByPostId(postId, pageRequest);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
                .isEqualTo(this.pagedResultTester.write(new PagedResult<>(postCommentPage)).getJson());
    }

    @Test
    void givenComment_whenGetCommentById_thenOk() throws Exception {
        //given
        when(this.commentService.findById(this.commentId)).thenReturn(this.comment);

        //when
        String url = String.format("/posts/%s/comments/%s", UUID.randomUUID(), this.commentId);
        MockHttpServletResponse response = this.mockMvc.perform(get(url).with(jwt()))
                .andReturn()
                .getResponse();

        //then
        verify(this.commentService).findById(this.commentId);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(this.commentTester.write(this.comment).getJson());
    }

    @Test
    void givenComment_whenDelete_thenNoContent() throws Exception {
        //given
        when(this.commentService.findById(this.commentId)).thenReturn(this.comment);

        //when
        String url = String.format("/posts/%s/comments/%s", UUID.randomUUID(), this.comment.getId());
        this.mockMvc.perform(delete(url).with(jwt().jwt(jwt -> jwt.subject(this.comment.getUser().getUserId()))))
                .andExpect(status().isNoContent());

        //then
        verify(this.commentService).findById(this.commentId);
        verify(this.commentService).deleteComment(this.commentId);
    }
}