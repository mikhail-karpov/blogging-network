package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostCreatedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.event.PostDeletedEvent;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PostServiceImpl.class})
@RecordApplicationEvents
class PostServiceImplEventTest {

    @MockBean
    private UserService userService;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void whenCreatePost_thenEventIsPublished() {
        //given
        UUID postId = UUID.randomUUID();
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(postId);
        when(this.postRepository.save(any())).thenReturn(post);

        //when
        assertEquals(postId, this.postService.createPost("user", "post content"));

        //then
        assertEquals(1L, this.applicationEvents.stream(PostCreatedEvent.class).count());
    }

    @Test
    void whenDeletePost_thenEventIsPublished() {
        //given
        UUID postId = UUID.randomUUID();
        PostProjection post = mock(PostProjection.class);
        when(post.getId()).thenReturn(postId);
        when(post.getUserId()).thenReturn("user");
        when(this.postRepository.findPostById(postId)).thenReturn(Optional.of(post));

        //when
        this.postService.deleteById(postId);

        //then
        verify(this.postRepository).deleteById(postId);
        assertEquals(1L, this.applicationEvents.stream(PostDeletedEvent.class).count());
    }

    @Test
    void whenDeleteNonExistingPost_thenNoEvent() {
        //given
        UUID postId = UUID.randomUUID();
        when(this.postRepository.findPostById(postId)).thenReturn(Optional.empty());

        //when
        assertThrows(ResourceNotFoundException.class, () -> this.postService.deleteById(postId));

        //then
        assertEquals(0L, this.applicationEvents.stream(PostDeletedEvent.class).count());
    }
}