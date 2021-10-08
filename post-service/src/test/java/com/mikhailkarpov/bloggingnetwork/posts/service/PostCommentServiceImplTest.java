package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostCommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @MockBean
    private PostService postService;

    @MockBean
    private PostCommentRepository commentRepository;

    private PostCommentServiceImpl postCommentService;

    @BeforeEach
    void setUp() {
        this.postCommentService = new PostCommentServiceImpl(this.postService, this.commentRepository);
    }

    private final Post post = EntityUtils.createRandomPost(35);
    private final PostComment comment = EntityUtils.createRandomPostComment(25);

    @Test
    void givenPost_whenAddComment_thenSaved() {
        //given
        UUID postId = post.getId();
        when(postService.findById(postId, true)).thenReturn(Optional.of(post));

        //when
        PostComment savedComment = postCommentService.addComment(postId, comment);

        //then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo(comment.getContent());

        verify(postService).findById(postId, true);
        verify(postService).save(any());
        verifyNoInteractions(commentRepository);
    }

    @Test
    void givenNoPost_whenAddComment_thenThrown() {
        //given
        UUID postId = UUID.randomUUID();
        when(postService.findById(postId, true)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> postCommentService.addComment(postId, comment))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postService).findById(postId, true);
        verifyNoMoreInteractions(postService);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void givenComments_whenFindAllByPostId_thenFound() {
        //given
        UUID postId = post.getId();
        PageRequest pageRequest = PageRequest.of(2, 4);
        Page<PostComment> commentPage = new PageImpl<>(Collections.singletonList(comment), pageRequest, 9L);
        when(commentRepository.findAllByPostId(postId, pageRequest)).thenReturn(commentPage);

        //when
        Page<PostComment> foundCommentPage = postCommentService.findAllByPostId(postId, pageRequest);

        //then
        assertThat(foundCommentPage).isNotNull();
        assertThat(foundCommentPage).usingRecursiveComparison().isEqualTo(commentPage);
        verify(commentRepository).findAllByPostId(postId, pageRequest);
        verifyNoInteractions(postService);
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
        verify(commentRepository).findById(commentId);
        verifyNoInteractions(postService);
    }

    @Test
    void givenComment_whenRemoveComment_thenRemoved() {
        //given
        UUID postId = post.getId();
        UUID commentId = comment.getId();

        //when
        postCommentService.removeComment(postId, commentId);

        //then
        verify(commentRepository).deleteById(commentId);
        verifyNoInteractions(postService);
    }
}