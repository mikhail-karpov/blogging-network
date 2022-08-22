package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.CommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import com.mikhailkarpov.bloggingnetwork.posts.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
public class CommentServiceImplTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @MockBean
    private UserService userService;

    private CommentServiceImpl postCommentService;

    @BeforeEach
    void setUp() {
        this.postCommentService =
                new CommentServiceImpl(this.postRepository, this.commentRepository, this.userService);
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void givenPost_whenCreateComment_thenCommentIsFound() {
        //given
        UUID postId = UUID.fromString("e7365159-8d52-4adb-9355-9787a63d945d");
        String userId = UUID.randomUUID().toString();
        String comment = RandomStringUtils.random(16);

        UserProfileDto user = new UserProfileDto(userId, "user-name");
        when(this.userService.getUserById(userId)).thenReturn(user);

        //when
        UUID commentId = this.postCommentService.createComment(postId, userId, comment);
        CommentDto foundComment = this.postCommentService.findById(commentId);

        //then
        assertThat(foundComment.getId()).isEqualTo(commentId.toString());
        assertThat(foundComment.getUser()).isEqualTo(user);
        assertThat(foundComment.getComment()).isEqualTo(comment);
        assertThat(foundComment.getCreatedDate()).isBefore(Instant.now());
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
        UUID commentId = UUID.fromString("4fecb4ee-7d00-43fa-9672-7cca75091fb7");

        //when
        this.postCommentService.deleteComment(commentId);

        //then
        assertThatThrownBy(() -> this.postCommentService.findById(commentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenNoComments_whenDeleteComment_thenException() {

        assertThatThrownBy(() -> this.postCommentService.deleteComment(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenComments_whenFindByPostId_thenFound() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");
        when(this.userService.getUserById(anyString())).thenReturn(mock(UserProfileDto.class));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<CommentDto> commentPage = this.postCommentService.findAllByPostId(postId, pageRequest);

        //then
        assertThat(commentPage.getTotalElements()).isEqualTo(2L);
        assertThat(commentPage.getContent().get(0).getComment()).isEqualTo("Second comment");
        assertThat(commentPage.getContent().get(1).getComment()).isEqualTo("First comment");
    }
}
