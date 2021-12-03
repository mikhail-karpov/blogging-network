package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import com.mikhailkarpov.bloggingnetwork.posts.domain.CommentProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenComments_whenGetCommentsProjectionByPostId_thenFound() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "userId"));
        Page<CommentProjection> comments = this.commentRepository.findAllByPostId(postId, pageRequest);

        //then
        assertThat(comments.getTotalElements()).isEqualTo(2L);
        assertThat(comments.getNumberOfElements()).isEqualTo(2);
        assertThat(comments.getContent().get(0).getUserId()).isEqualTo("user-4");
        assertThat(comments.getContent().get(1).getUserId()).isEqualTo("user-3");
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenComments_whenGetById_thenFound() {
        //given
        UUID commentId = UUID.fromString("4fecb4ee-7d00-43fa-9672-7cca75091fb7");

        //when
        Optional<CommentProjection> comment = this.commentRepository.findCommentById(commentId);

        //then
        assertThat(comment).isPresent();
    }
}