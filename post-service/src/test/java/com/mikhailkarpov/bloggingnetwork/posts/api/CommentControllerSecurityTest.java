package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import com.mikhailkarpov.bloggingnetwork.posts.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
class CommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CommentService commentService;

    private final UUID id = UUID.randomUUID();

    @Test
    void givenNoAuth_whenHitEndpoints_thenUnauthorized() throws Exception {

        mockMvc.perform(post("/posts/{id}/comments", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts/{id}/comments", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts/{id}/comments/{commentId}", id, id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/posts/{id}/comments/{commentId}", id, id))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(commentService);
    }

    @Test
    void givenNoAuthority_whenDeleteComment_thenForbidden() throws Exception {
        //given
        CommentDto comment = CommentDto.builder()
                .id(id.toString())
                .comment("Comment")
                .user(new UserProfileDto("userId", "username"))
                .createdDate(Instant.now())
                .build();

        when(commentService.findById(id)).thenReturn(comment);

        //when
        mockMvc.perform(delete("/posts/{id}/comments/{commentId}", id, id)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject("not owner"))))
                .andExpect(status().isForbidden());

        verify(commentService).findById(id);
        verifyNoMoreInteractions(commentService);
    }
}