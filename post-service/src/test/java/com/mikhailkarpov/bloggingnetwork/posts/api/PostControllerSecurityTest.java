package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.TestSecurityConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerSecurityTest extends AbstractControllerTest {

    @MockBean
    private PostService postService;

    private final UUID id = UUID.randomUUID();

    @Test
    void givenNoAuth_whenHitEndpoints_thenUnauthorized() throws Exception {

        mockMvc.perform(post("/posts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts/{id}", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/posts/{postId}/users/{userId}", id, id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/posts/{id}", id))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(postService);
    }

    @Test
    void givenNotOwner_whenDeletePost_thenForbidden() throws Exception {
        //given
        UUID postId = UUID.randomUUID();
        Post post = new Post("not owner", "Post content");

        when(postService.findById(postId, false)).thenReturn(Optional.of(post));

        //when
        mockMvc.perform(delete("/posts/{id}", postId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        //then
        verify(postService).findById(postId, false);
        verifyNoMoreInteractions(postService);
    }
}