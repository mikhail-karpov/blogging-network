package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
        PostDto post = Mockito.mock(PostDto.class);
        UserProfileDto user = Mockito.mock(UserProfileDto.class);

        when(postService.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn("user");

        //when
        mockMvc.perform(delete("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject("not-owner"))))
                .andExpect(status().isForbidden());

        //then
        verify(postService).findById(postId);
        verifyNoMoreInteractions(postService);
    }
}