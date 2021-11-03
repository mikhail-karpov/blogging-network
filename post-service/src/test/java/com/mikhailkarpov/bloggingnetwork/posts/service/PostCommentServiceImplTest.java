package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostCommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PostCommentServiceImplTest {

    @Mock
    private PostService postService;

    @Mock
    private PostCommentRepository commentRepository;

    @InjectMocks
    private PostCommentServiceImpl postCommentService;

    private final Post post = EntityUtils.createRandomPost(35);
    private final UUID postId = post.getId();
    private final PostComment comment = EntityUtils.createRandomPostComment(25);
    private final PageRequest pageRequest = PageRequest.of(2, 4);
    private final Page<PostComment> commentPage =
            new PageImpl<>(Collections.singletonList(comment), pageRequest, 9L);

    @Test
    void givenPost_whenAddComment_thenSaved() {
        //given
        when(postService.findById(postId, true)).thenReturn(Optional.of(post));

        //when
        PostComment savedComment = postCommentService.addComment(postId, comment);

        //then
        assertThat(savedComment).isEqualTo(comment);

        verify(postService).findById(postId, true);
        verify(postService).save(post);
    }

    @Test
    void givenNoPost_whenAddComment_thenThrown() {
        //given
        when(postService.findById(postId, true)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> postCommentService.addComment(postId, comment))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postService).findById(postId, true);
        verifyNoMoreInteractions(postService);
    }

    @Test
    void givenComments_whenFindAllByPostId_thenFound() {
        //given
        when(commentRepository.findAllByPostId(postId, pageRequest)).thenReturn(commentPage);

        //when
        Page<PostComment> foundCommentPage = postCommentService.findAllByPostId(postId, pageRequest);

        //then
        assertThat(foundCommentPage).usingRecursiveComparison().isEqualTo(commentPage);
    }

    @Test
    void givenPostComment_whenFindById_thenFound() {
        //given
        UUID commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        //when
        Optional<PostComment> foundComment = postCommentService.findById(commentId);

        //then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get()).isEqualTo(comment);
    }

    @Test
    void givenComment_whenRemoveComment_thenRemoved() {
        //given
        UUID commentId = comment.getId();

        //when
        postCommentService.removeComment(postId, commentId);

        //then
        verify(commentRepository).deleteById(commentId);
    }
}