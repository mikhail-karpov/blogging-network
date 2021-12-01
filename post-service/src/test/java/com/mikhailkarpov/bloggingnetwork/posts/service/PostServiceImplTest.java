package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostProjectionTestImpl;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
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

import java.time.Instant;
import java.util.Arrays;
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
    private UserService userService;

    @InjectMocks
    private PostServiceImpl postService;

    @Captor
    private ArgumentCaptor<Post> postArgumentCaptor;

    private final PostProjection post = PostProjectionTestImpl.builder()
            .id(UUID.randomUUID())
            .userId("userId")
            .content("Post content")
            .createdDate(Instant.now())
            .build();

    private final UserProfileDto user = new UserProfileDto("userId", "userName");

    @Test
    void givenPost_whenDeleteById_thenDelete() {
        //given
        when(this.postRepository.existsById(any())).thenReturn(true);

        //when
        UUID postId = UUID.randomUUID();
        this.postService.deleteById(postId);

        //then
        verify(this.postRepository).deleteById(postId);
    }

    @Test
    void givenNoPost_whenDeleteById_thenThrows() {
        //given
        when(postRepository.existsById(any())).thenReturn(false);

        //when
        UUID id = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> postService.deleteById(id));

        //then
        verify(this.postRepository).existsById(id);
        verifyNoMoreInteractions(this.postRepository);
    }

    @Test
    void test_findAllByUserId() {
        //given
        PageRequest pageRequest = PageRequest.of(1, 3);
        Page<PostProjection> postPage = new PageImpl<>(Arrays.asList(this.post), pageRequest, 4L);

        when(this.postRepository.findPostsByUserId("userId", pageRequest)).thenReturn(postPage);
        when(this.userService.getUserById("userId")).thenReturn(user);

        //when
        Page<PostDto> foundPage = postService.findAllByUserId("userId", pageRequest);

        //then
        assertThat(foundPage.getTotalElements()).isEqualTo(4L);
        assertThat(foundPage.getNumberOfElements()).isEqualTo(1);

        PostDto postDto = foundPage.getContent().get(0);
        assertThat(postDto.getId()).isEqualTo(post.getId().toString());
        assertThat(postDto.getContent()).isEqualTo("Post content");
        assertThat(postDto.getCreatedDate()).isEqualTo(post.getCreatedDate());
        assertThat(postDto.getUser()).isEqualTo(user);
    }

    @Test
    void test_findById() {
        //given
        UUID postId = this.post.getId();
        when(this.postRepository.findPostById(postId)).thenReturn(Optional.of(this.post));
        when(this.userService.getUserById(this.post.getUserId())).thenReturn(this.user);

        //when
        Optional<PostDto> found = postService.findById(postId);

        //then
        assertThat(found).isPresent();
        assertThat(found.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void test_save() {
        //given
        Post post = new Post("userId", "Post content");
        when(postRepository.save(any())).thenReturn(post);

        //when
        UUID postId = this.postService.createPost("userId", "Post content");

        //then
        assertThat(postId).isEqualTo(post.getId());
        verify(this.postRepository).save(this.postArgumentCaptor.capture());
        assertThat(this.postArgumentCaptor.getValue().getUserId()).isEqualTo("userId");
        assertThat(this.postArgumentCaptor.getValue().getContent()).isEqualTo("Post content");
    }
}