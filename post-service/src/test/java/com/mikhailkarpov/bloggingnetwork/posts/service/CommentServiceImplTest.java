package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.CommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
public class CommentServiceImplTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    CommentServiceImpl postCommentService;

    @BeforeEach
    void setUp() {
        this.postCommentService = new CommentServiceImpl(postRepository, commentRepository);
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void givenPost_whenCreateComment_thenCommentIsFound() {
        //given
        UUID postId = UUID.fromString("e7365159-8d52-4adb-9355-9787a63d945d");
        String userId = UUID.randomUUID().toString();
        String comment = RandomStringUtils.random(16);

        //when
        UUID commentId = this.postCommentService.createComment(postId, userId, comment);
        Optional<Comment> foundComment = this.postCommentService.findById(commentId);

        //then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getId()).isEqualTo(commentId);
        assertThat(foundComment.get().getUserId()).isEqualTo(userId);
        assertThat(foundComment.get().getComment()).isEqualTo(comment);
        assertThat(foundComment.get().getCreatedDate()).isBefore(Instant.now());
    }

    @Test
    void givenNoPost_whenCreateComment_thenException() {
        //given
        UUID postId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        String comment = RandomStringUtils.random(16);

        //when
        assertThatThrownBy(() -> this.postCommentService.createComment(postId, userId, comment))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenComments_whenDeleteComment_thenDeleted() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");
        UUID commentId = UUID.fromString("4fecb4ee-7d00-43fa-9672-7cca75091fb7");
        assertThat(this.postCommentService.findById(commentId)).isPresent();

        //when
        this.postCommentService.removeComment(postId, commentId);

        //then
        assertThat(this.postCommentService.findById(commentId)).isEmpty();
    }

    @Test
    void givenNoComments_whenDeleteComment_thenException() {
        //given
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        //when
        assertThatThrownBy(() -> this.postCommentService.removeComment(postId, commentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenComments_whenFindByPostId_thenFound() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<Comment> commentPage = this.postCommentService.findAllByPostId(postId, pageRequest);

        //then
        assertThat(commentPage.getTotalElements()).isEqualTo(2L);
        assertThat(commentPage.getContent().get(0).getComment()).isEqualTo("Second comment");
        assertThat(commentPage.getContent().get(1).getComment()).isEqualTo("First comment");
    }
}
