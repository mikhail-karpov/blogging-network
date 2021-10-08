package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostCommentServiceImplIT extends AbstractIT {

    @Autowired
    private PostCommentServiceImpl postCommentService;

    @Autowired
    private PostService postService;

    private final Post post = EntityUtils.createRandomPost(25);

    @BeforeEach
    void savePost() {
        postService.save(post);
    }

    @Test
    void givenPost_whenAddCommentsAndGetComments_thenFound() {
        //given
        UUID postId = post.getId();

        //when
        for (int i = 0; i < 10; i++) {
            PostComment comment = EntityUtils.createRandomPostComment(14);
            postCommentService.addComment(postId, comment);
        }
        Page<PostComment> commentPage = postCommentService.findAllByPostId(postId, PageRequest.of(2, 4));

        //then
        assertThat(commentPage).isNotNull();
        assertThat(commentPage.getTotalElements()).isEqualTo(10L);
        assertThat(commentPage.getTotalPages()).isEqualTo(3);
        assertThat(commentPage.getNumber()).isEqualTo(2);
        assertThat(commentPage.getSize()).isEqualTo(4);
        assertThat(commentPage.getContent().size()).isEqualTo(2);
    }

    @Test
    void givenNoPost_whenAddComment_thenThrown() {
        //given
        UUID postId = UUID.randomUUID();
        PostComment comment = EntityUtils.createRandomPostComment(45);

        //when
        assertThrows(ResourceNotFoundException.class, () -> postCommentService.addComment(postId, comment));
    }

    @Test
    void givenComment_whenFindById_thenFound() {
        //given
        UUID postId = post.getId();
        PostComment comment = EntityUtils.createRandomPostComment(45);

        //when
        PostComment savedComment = postCommentService.addComment(postId, comment);
        Optional<PostComment> found = postCommentService.findById(savedComment.getId());

        //then
        assertThat(found).isPresent();
    }

    @Test
    void givenNoComment_whenFindById_thenNotFound() {
        //given
        UUID commentId = UUID.randomUUID();

        //when
        Optional<PostComment> notFoundComment = postCommentService.findById(commentId);

        //then
        assertThat(notFoundComment).isEmpty();
    }

    @Test
    void givenComment_whenRemove_thenDeleted() {
        //given
        UUID postId = post.getId();
        PostComment comment = EntityUtils.createRandomPostComment(32);

        //when
        postCommentService.addComment(postId, comment);
        postCommentService.removeComment(postId, comment.getId());
        Optional<PostComment> notFoundComment = postCommentService.findById(comment.getId());
        Page<PostComment> commentPage = postCommentService.findAllByPostId(postId, PageRequest.of(0, 1));

        //then
        assertThat(notFoundComment).isEmpty();
        assertThat(commentPage).isEmpty();
    }

    @Test
    void givenNoComment_whenRemove_thenThrown() {
        //given
        UUID postId = post.getId();
        UUID commentId = UUID.randomUUID();

        //when
        assertThrows(ResourceNotFoundException.class, () -> postCommentService.removeComment(postId, commentId));
    }
}