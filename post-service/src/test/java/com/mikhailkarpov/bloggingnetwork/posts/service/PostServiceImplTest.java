package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PostServiceImplTest {

    @MockBean
    private PostRepository postRepository;

    private PostServiceImpl postService;

    private final PageRequest pageRequest = PageRequest.of(1, 3);

    @BeforeEach
    void setUpPostService() {
        this.postService = new PostServiceImpl(postRepository);
    }

    @Test
    void givenPost_whenDeleteById_thenDelete() {
        //given
        UUID id = UUID.randomUUID();
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(id);
        when(postRepository.findByIdWithComments(id)).thenReturn(Optional.of(post));

        //when
        postService.deleteById(id);

        //then
        verify(postRepository).findByIdWithComments(id);
        verify(postRepository).deleteById(id);
    }

    @Test
    void givenNoPost_whenDeleteById_thenThrows() {
        //given
        UUID id = UUID.randomUUID();
        when(postRepository.findByIdWithComments(id)).thenReturn(Optional.empty());

        //when
        assertThrows(ResourceNotFoundException.class, () -> postService.deleteById(id));

        //then
        verify(postRepository).findByIdWithComments(id);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void findAll() {
        //given
        when(postRepository.findAll(pageRequest)).thenReturn(mock(Page.class));

        //when
        Page<Post> postPage = postService.findAll(pageRequest);

        //then
        verify(postRepository).findAll(pageRequest);
    }

    @Test
    void findAllByUserId() {
        //given
        String userId = UUID.randomUUID().toString();
        when(postRepository.findByUserId(userId, pageRequest)).thenReturn(mock(Page.class));

        //when
        Page<Post> postPage = postService.findAllByUserId(userId, pageRequest);

        //then
        verify(postRepository).findByUserId(userId, pageRequest);
    }

    @Test
    void findById() {
        //given
        UUID id = UUID.randomUUID();
        when(postRepository.findById(id)).thenReturn(Optional.of(mock(Post.class)));

        //when
        Optional<Post> post = postService.findById(id);

        //then
        verify(postRepository).findById(id);
    }

    @Test
    void findByIdWithComments() {
        //given
        UUID id = UUID.randomUUID();
        when(postRepository.findByIdWithComments(id)).thenReturn(Optional.of(mock(Post.class)));

        //when
        Optional<Post> post = postService.findById(id, true);

        //then
        verify(postRepository).findByIdWithComments(id);
    }

    @Test
    void save() {
        //given
        Post post = mock(Post.class);
        when(postRepository.save(post)).thenReturn(post);

        //when
        Post saved = postService.save(post);

        //then
        verify(postRepository).save(post);
    }
}