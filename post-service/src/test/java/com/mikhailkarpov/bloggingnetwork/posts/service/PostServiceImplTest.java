package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEventPublisher;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostEventPublisher eventPublisher;

    @InjectMocks
    private PostServiceImpl postService;

    @Captor
    private ArgumentCaptor<PostEvent> eventArgumentCaptor;

    private final Post post = new Post("user", "content");
    private final UUID postId = post.getId();
    private final PageRequest pageRequest = PageRequest.of(1, 3);
    private final PageImpl<Post> postPage =
            new PageImpl<>(Collections.singletonList(post), pageRequest, 4L);

    @Test
    void givenPost_whenDeleteById_thenDelete() {
        //given
        when(postRepository.findByIdWithComments(postId)).thenReturn(Optional.of(post));

        //when
        postService.deleteById(postId);

        //then
        verify(postRepository).findByIdWithComments(postId);
        verify(postRepository).delete(post);
        verify(eventPublisher).publish(eventArgumentCaptor.capture());

        PostEvent event = eventArgumentCaptor.getValue();
        assertThat(event.getPostId()).isEqualTo(postId.toString());
        assertThat(event.getAuthorId()).isEqualTo(post.getUserId());
        assertThat(event.getStatus()).isEqualTo(EventStatus.DELETED);
    }

    @Test
    void givenNoPost_whenDeleteById_thenThrows() {
        //given
        when(postRepository.findByIdWithComments(any())).thenReturn(Optional.empty());

        //when
        UUID id = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> postService.deleteById(id));

        //then
        verify(postRepository).findByIdWithComments(id);
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void test_findAll() {
        //given
        when(postRepository.findAll(pageRequest)).thenReturn(postPage);

        //when
        Page<Post> foundPage = postService.findAll(pageRequest);

        //then
        Assertions.assertThat(foundPage).usingRecursiveComparison().isEqualTo(postPage);
    }

    @Test
    void test_findAllByUserId() {
        //given
        when(postRepository.findByUserId("userId", pageRequest)).thenReturn(postPage);

        //when
        Page<Post> foundPage = postService.findAllByUserId("userId", pageRequest);

        //then
        Assertions.assertThat(foundPage).usingRecursiveComparison().isEqualTo(postPage);
    }

    @Test
    void test_findById() {
        //given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        //when
        Optional<Post> found = postService.findById(postId);

        //then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(post);
    }

    @Test
    void test_findByIdWithComments() {
        //given
        when(postRepository.findByIdWithComments(postId)).thenReturn(Optional.of(post));

        //when
        Optional<Post> found = postService.findById(postId, true);

        //then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(post);
    }

    @Test
    void test_save() {
        //given
        when(postRepository.save(post)).thenReturn(post);

        //when
        Post saved = postService.save(post);

        //then
        verify(eventPublisher).publish(eventArgumentCaptor.capture());
        PostEvent event = eventArgumentCaptor.getValue();

        assertThat(saved).isEqualTo(post);
        assertThat(event.getPostId()).isEqualTo(postId.toString());
        assertThat(event.getAuthorId()).isEqualTo(post.getUserId());
        assertThat(event.getStatus()).isEqualTo(EventStatus.CREATED);
    }
}