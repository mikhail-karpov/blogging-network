package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostServiceImplIT extends AbstractIT {

    @Autowired
    private PostServiceImpl postService;

    @Test
    void givenPost_whenSave_thenFound() {
        //given
        Post post = EntityUtils.createRandomPost(30);

        //when
        Post savedPost = postService.save(post);

        //then
        Optional<Post> foundPost = postService.findById(post.getId());

        assertThat(savedPost).isEqualTo(savedPost);
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get()).isEqualTo(post);
    }

    @Test
    void givenPost_whenSaveAndDelete_thenNotFound() {
        //given
        Post post = EntityUtils.createRandomPost(25);

        //when
        postService.save(post);
        postService.deleteById(post.getId());

        //then
        assertThat(postService.findById(post.getId())).isEmpty();
    }

    @Test
    void givenPosts_whenFindAll_thenFound() {
        //given
        for (int i = 0; i < 10; i++) {
            Post post = EntityUtils.createRandomPost(45);
            postService.save(post);
        }

        //when
        PageRequest pageRequest = PageRequest.of(2, 4);
        Page<Post> postPage = postService.findAll(pageRequest);

        //then
        assertThat(postPage).isNotNull();
        assertThat(postPage.getTotalPages()).isEqualTo(3);
        assertThat(postPage.getTotalElements()).isEqualTo(10L);
        assertThat(postPage.getNumber()).isEqualTo(2);
        assertThat(postPage.getSize()).isEqualTo(4);
        assertThat(postPage.getContent().size()).isEqualTo(2);
    }

    @Test
    void givenUserPosts_whenFindByUserId_thenFound() {
        //given
        String userId = UUID.randomUUID().toString();
        for (int i = 0; i < 10; i++) {
            Post post = new Post(userId, RandomStringUtils.randomAlphabetic(45));
            postService.save(post);
        }

        //when
        PageRequest pageRequest = PageRequest.of(2, 4);
        Page<Post> postPage = postService.findAllByUserId(userId, pageRequest);

        //then
        assertThat(postPage).isNotNull();
        assertThat(postPage.getTotalPages()).isEqualTo(3);
        assertThat(postPage.getTotalElements()).isEqualTo(10L);
        assertThat(postPage.getNumber()).isEqualTo(2);
        assertThat(postPage.getSize()).isEqualTo(4);
        assertThat(postPage.getContent().size()).isEqualTo(2);
    }

    @Test
    void givenPostWithComments_whenFindWithComments_thenFound() {
        //given
        Post post = EntityUtils.createRandomPost(35);
        for (int i = 0; i < 10; i++) {
            PostComment comment = EntityUtils.createRandomPostComment(20);
            post.addComment(comment);
        }
        postService.save(post);

        //when
        Optional<Post> foundPost = postService.findById(post.getId(), true);

        //then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get()).hasNoNullFieldsOrProperties();
        assertThat(foundPost.get()).usingRecursiveComparison().isEqualTo(post);
        assertThat(foundPost.get().getPostComments().size()).isEqualTo(10);
    }
}