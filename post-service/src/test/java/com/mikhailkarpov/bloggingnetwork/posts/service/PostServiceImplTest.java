package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.repository.CommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
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
import static org.mockito.Mockito.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class PostServiceImplTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<PostEvent> eventArgumentCaptor;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        this.postService = new PostServiceImpl(this.postRepository, this.userService, this.eventPublisher);
    }

    @Test
    void whenCreatePost_thenEventIsPublishedAndPostFound() {
        //given
        String userId = UUID.randomUUID().toString();
        String postContent = RandomStringUtils.randomAlphabetic(35);

        UserProfileDto user = new UserProfileDto(userId, "user-name");
        when(this.userService.getUserById(userId)).thenReturn(user);

        //when
        UUID postId = this.postService.createPost(userId, postContent);
        Optional<PostDto> foundPost = this.postService.findById(postId);

        //then
        assertThat(foundPost).isPresent();

        PostDto postDto = foundPost.get();
        assertThat(postDto.getId()).isEqualTo(postId.toString());
        assertThat(postDto.getContent()).isEqualTo(postContent);
        assertThat(postDto.getCreatedDate()).isBefore(Instant.now());
        assertThat(postDto.getUser()).isEqualTo(user);

        verify(this.eventPublisher).publishEvent(this.eventArgumentCaptor.capture());

        PostEvent event = this.eventArgumentCaptor.getValue();
        assertThat(event.getAuthorId()).isEqualTo(userId);
        assertThat(event.getPostId()).isEqualTo(postId.toString());
        assertThat(event.getStatus()).isEqualTo(EventStatus.CREATED);
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_posts.sql", "/db_scripts/insert_comments.sql"})
    void givenPosts_whenDeleteById_thenDeletedAndEventPublished() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");
        assertThat(this.postService.findById(postId)).isPresent();

        //when
        this.postService.deleteById(postId);

        //then
        assertThat(this.postService.findById(postId)).isEmpty();
        verify(this.eventPublisher).publishEvent(this.eventArgumentCaptor.capture());

        PostEvent event = this.eventArgumentCaptor.getValue();
        assertThat(event.getAuthorId()).isEqualTo("user-1");
        assertThat(event.getPostId()).isEqualTo("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");
        assertThat(event.getStatus()).isEqualTo(EventStatus.DELETED);

        PageRequest pageRequest = PageRequest.of(0, 3);
        assertThat(this.commentRepository.findAllByPostId(postId, pageRequest).getTotalElements()).isEqualTo(0L);
    }

    @Test
    void givenNoPost_whenDeleteById_thenException() {
        //given
        UUID postId = UUID.randomUUID();

        //then
        assertThatThrownBy(() -> this.postService.deleteById(postId)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void givenPosts_whenFindByUserId_thenFound() {
        //given
        String userId = "user-1";
        UserProfileDto user = new UserProfileDto(userId, "user-name");
        when(this.userService.getUserById(userId)).thenReturn(user);

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "content"));
        Page<PostDto> postPage = this.postService.findAllByUserId(userId, pageRequest);

        //then
        assertThat(postPage.getTotalElements()).isEqualTo(2L);
        assertThat(postPage.getNumberOfElements()).isEqualTo(2);
        assertThat(postPage.getContent().get(0).getContent()).isEqualTo("Second post by user-1");
        assertThat(postPage.getContent().get(0).getUser()).isEqualTo(user);
        assertThat(postPage.getContent().get(1).getContent()).isEqualTo("Hello world!");
        assertThat(postPage.getContent().get(1).getUser()).isEqualTo(user);
    }
}