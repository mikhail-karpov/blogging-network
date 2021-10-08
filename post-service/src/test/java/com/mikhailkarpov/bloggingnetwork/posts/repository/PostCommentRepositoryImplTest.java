package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostCommentRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    private PostCommentRepositoryImpl commentRepository;

    private UUID postId;

    @BeforeEach
    void savePost() {
        commentRepository = new PostCommentRepositoryImpl(entityManager.getEntityManager());

        Post post = new Post("user", "Post content");
        postId = entityManager.persistAndGetId(post, UUID.class);
    }

    @Test
    void deleteById() {
        //given
        PostComment comment = createRandomPostComment();
        Post post = entityManager.find(Post.class, postId);
        post.addComment(comment);
        entityManager.flush();

        //when
        UUID commentId = comment.getId();
        commentRepository.deleteById(commentId);
        Optional<PostComment> foundComment = commentRepository.findById(commentId);

        //then
        assertThat(foundComment).isEmpty();
    }

    @Test
    void findAllByPostId() {
        //given
        Post post = entityManager.find(Post.class, postId);

        for (int i = 0; i < 10; i++) {
            PostComment postComment = createRandomPostComment();
            post.addComment(postComment);
        }
        entityManager.flush();

        //when
        Page<PostComment> comments = commentRepository.findAllByPostId(postId, PageRequest.of(2, 4));

        //then
        assertThat(comments).isNotNull();
        assertThat(comments.getTotalElements()).isEqualTo(10L);
        assertThat(comments.getTotalPages()).isEqualTo(3);
        assertThat(comments.getNumber()).isEqualTo(2);
        assertThat(comments.getSize()).isEqualTo(4);
        assertThat(comments.getContent().size()).isEqualTo(2);
    }

    @Test
    void findById() {
        //given
        PostComment comment = createRandomPostComment();
        Post post = entityManager.find(Post.class, postId);
        post.addComment(comment);
        entityManager.flush();

        //when
        Optional<PostComment> foundComment = commentRepository.findById(comment.getId());

        //then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get()).usingRecursiveComparison().isEqualTo(comment);
    }

    private PostComment createRandomPostComment() {
        String userId = RandomStringUtils.randomAlphabetic(15);
        String comment = RandomStringUtils.randomAlphabetic(25);
        return new PostComment(userId, comment);
    }
}