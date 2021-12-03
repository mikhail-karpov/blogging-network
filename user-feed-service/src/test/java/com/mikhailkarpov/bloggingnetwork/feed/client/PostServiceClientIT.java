package com.mikhailkarpov.bloggingnetwork.feed.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.config.MockPostServiceConfig;
import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserProfile;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = MockPostServiceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PostServiceClientIT extends AbstractIT {

    @Autowired
    private List<WireMockServer> services;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostServiceClient postServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void contextLoads() {
        for (WireMockServer service : services) {
            service.resetAll();
        }
    }

    @Test
    void circuitBreakerShouldExists() {
        CircuitBreaker circuitBreaker = this.circuitBreakerRegistry.circuitBreaker("post-service");
        assertThat(circuitBreaker).isNotNull();
    }

    @Test
    void givenOkResponse_whenGetPostById_thenPresent() throws JsonProcessingException {
        //given
        UserProfile profile = new UserProfile("userId", "user");
        Post post = Post.builder()
                .id("postId")
                .content("Post content")
                .createdDate(LocalDateTime.of(2021, 10, 13, 15, 35, 43))
                .user(profile)
                .build();

        for (WireMockServer service : services) {
            service.stubFor(get("/posts/found")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(this.objectMapper.writeValueAsString(post))));
        }

        //when
        Optional<Post> foundPost = this.postServiceClient.getPostById("found");

        //then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get()).usingRecursiveComparison().isEqualTo(post);
    }

    @Test
    void givenNotFoundResponse_whenGetPostById_thenEmpty() {
        //given
        for (WireMockServer service : services) {
            service.stubFor(get("/posts/not-found")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(aResponse()
                            .withStatus(404)));
        }

        //when
        Optional<Post> post = this.postServiceClient.getPostById("not-found");

        //then
        assertThat(post).isEmpty();
    }

    @Test
    void givenServerError_whenGetById_thenFallbackIsCalled() {
        //given
        for (WireMockServer service : services) {
            service.stubFor(get("/posts/error")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(serverError()));
        }

        //when
        Optional<Post> post = this.postServiceClient.getPostById("error");

        //then
        assertThat(post).isPresent();
        assertThat(post.get().getId()).isEqualTo("error");
        assertThat(post.get().getContent()).isEqualTo(PostServiceClientFallback.DEFAULT);
        assertThat(post.get().getUser()).isNotNull();
        assertThat(post.get().getUser()).hasNoNullFieldsOrProperties();
        assertThat(post.get().getCreatedDate()).isNotNull();
    }
}