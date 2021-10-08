package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
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
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    void givenUser_whenSavePostsAndFindByUserId_thenFound() {
        //given
        String userId = UUID.randomUUID().toString();
        for (int i = 0; i < 10; i++) {
            Post post = new Post(userId, RandomStringUtils.randomAlphabetic(25));
            entityManager.persist(post);
        }
        entityManager.flush();

        //when
        Page<Post> posts = postRepository.findByUserId(userId, PageRequest.of(2, 4));

        //then
        assertThat(posts.getTotalElements()).isEqualTo(10L);
        assertThat(posts.getSize()).isEqualTo(4);
        assertThat(posts.getNumber()).isEqualTo(2);
        assertThat(posts.getContent()).isNotNull();
        assertThat(posts.getContent().size()).isEqualTo(2);
    }

    @Test
    void givenPostWithComments_whenFindWithComments_thenFound() {
        //given
        Post post = createRandomPostWithComments(10);
        UUID postId = entityManager.persistAndGetId(post, UUID.class);
        entityManager.flush();

        //when
        Optional<Post> foundPost = postRepository.findByIdWithComments(postId);

        //then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getPostComments().size()).isEqualTo(10);
    }

    @Test
    void givenPostAndComments_whenFindCommentsByPostId_thenFound() {
        //given
        Post post = createRandomPostWithComments(10);
        UUID postId = entityManager.persistAndGetId(post, UUID.class);
        entityManager.flush();

        //when
        PageRequest pageRequest = PageRequest.of(2, 4);
        Page<PostComment> comments = postRepository.findCommentsByPostId(postId, pageRequest);

        //then
        assertThat(comments).isNotNull();
        assertThat(comments.getTotalElements()).isEqualTo(10L);
        assertThat(comments.getTotalPages()).isEqualTo(3);
        assertThat(comments.getNumber()).isEqualTo(2);
        assertThat(comments.getContent().size()).isEqualTo(2);
    }

    private Post createRandomPostWithComments(int commentsCount) {
        assertThat(commentsCount).isGreaterThan(0);

        Post post = EntityUtils.createRandomPost(25);
        for (int i = 0; i < commentsCount; i++) {
            PostComment postComment = EntityUtils.createRandomPostComment(15);
            post.addComment(postComment);
        }
        return post;
    }
}