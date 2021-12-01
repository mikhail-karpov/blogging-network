package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @MockBean
    private PostEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<PostEvent> eventArgumentCaptor;

    @Test
    void whenSavePost_thenPostIsSavedAndEventIsPublished() {
        //given
        Post post = new Post("user", "post content");

        //when
        Post savedPost = this.postRepository.save(post);
        this.entityManager.flush();
        Optional<Post> foundPost = this.postRepository.findById(post.getId());

        //then
        assertThat(savedPost.getId()).isEqualTo(post.getId());
        assertThat(savedPost.getUserId()).isEqualTo("user");
        assertThat(savedPost.getContent()).isEqualTo("post content");
        assertThat(savedPost.getCreatedDate()).isNotNull();

        assertThat(foundPost).isPresent();
        assertThat(foundPost.get()).isEqualTo(savedPost);

        verify(this.eventPublisher).publish(this.eventArgumentCaptor.capture());

        PostEvent event = this.eventArgumentCaptor.getValue();
        assertThat(event.getAuthorId()).isEqualTo("user");
        assertThat(event.getPostId()).isEqualTo(post.getId().toString());
        assertThat(event.getStatus()).isEqualTo(EventStatus.CREATED);
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void whenDeletePost_thenPostIsRemovedAndEventIsPublished() {
        //given
        UUID postId = UUID.fromString("e7365159-8d52-4adb-9355-9787a63d945d");

        //when
        this.postRepository.deleteById(postId);
        this.entityManager.flush();

        //then
        assertThat(this.postRepository.existsById(postId)).isFalse();
        verify(this.eventPublisher).publish(this.eventArgumentCaptor.capture());

        PostEvent event = this.eventArgumentCaptor.getValue();
        assertThat(event.getAuthorId()).isEqualTo("user-2");
        assertThat(event.getPostId()).isEqualTo(postId.toString());
        assertThat(event.getStatus()).isEqualTo(EventStatus.DELETED);
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void givenPost_whenFindPostProjectionById_thenFound() {
        //given
        UUID postId = UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3");

        //when
        Optional<PostProjection> post = this.postRepository.findPostById(postId);

        //then
        assertThat(post).isPresent();
        assertThat(post.get().getId()).isEqualTo(postId);
        assertThat(post.get().getUserId()).isEqualTo("user-1");
        assertThat(post.get().getContent()).isEqualTo("Hello world!");
    }

    @Test
    @Sql(scripts = "/db_scripts/insert_posts.sql")
    void givenPosts_whenFindPostProjectionByUserId_thenFound() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PostProjection> posts = this.postRepository.findPostsByUserId("user-1", pageRequest);

        //then
        assertThat(posts.getTotalPages()).isEqualTo(1);
        assertThat(posts.getTotalElements()).isEqualTo(2L);
        assertThat(posts.getNumberOfElements()).isEqualTo(2);
        assertThat(posts.getContent().get(0).getId()).isEqualTo(UUID.fromString("a41d09c1-7aef-4328-8fc4-141092133f88"));
        assertThat(posts.getContent().get(1).getId()).isEqualTo(UUID.fromString("32ccebc5-22c8-4d39-9044-aee9ec4e30f3"));
    }
}