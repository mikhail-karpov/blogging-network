package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostCommentController.class)
class PostCommentControllerSecurityTest extends AbstractControllerTest {

    @MockBean
    private PostCommentService postCommentService;

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

        verifyNoInteractions(postCommentService);
    }

    @Test
    void givenNoAuthority_whenDeleteComment_thenForbidden() throws Exception {
        //given
        PostComment comment = new PostComment("owner", "Post comment");
        when(postCommentService.findById(id)).thenReturn(Optional.of(comment));

        //when
        mockMvc.perform(delete("/posts/{id}/comments/{commentId}", id, id)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject("not owner"))))
                .andExpect(status().isForbidden());

        verify(postCommentService).findById(id);
        verifyNoMoreInteractions(postCommentService);
    }
}