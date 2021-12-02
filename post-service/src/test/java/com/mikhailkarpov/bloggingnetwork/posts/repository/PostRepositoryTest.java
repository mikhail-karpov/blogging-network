package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

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