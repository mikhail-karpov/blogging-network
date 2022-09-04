package com.mikhailkarpov.bloggingnetwork.feed.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.config.MockPostServiceConfig;
import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import com.mikhailkarpov.bloggingnetwork.feed.model.UserProfile;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = MockPostServiceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PostServiceClientTest extends AbstractIT {

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
        assertNotNull(circuitBreaker);
    }

    @Test
    void givenOkResponse_whenGetPostById_thenPresent() throws JsonProcessingException {
        //given
        Post post = Post.builder()
                .id("postId")
                .content("Post content")
                .createdDate(LocalDateTime.of(2021, 10, 13, 15, 35, 43))
                .user(new UserProfile("userId", "user"))
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
        assertTrue(foundPost.isPresent());
        assertEquals(post, foundPost.get());
    }

    @Test
    void givenNotFoundResponse_whenGetPostById_thenEmpty() {
        for (WireMockServer service : services) {
            service.stubFor(get("/posts/not-found")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(aResponse()
                            .withStatus(404)));
        }

        assertFalse(this.postServiceClient.getPostById("not-found").isPresent());
    }

    @Test
    void givenServerError_whenGetById_thenFallbackIsCalled() {
        for (WireMockServer service : services) {
            service.stubFor(get("/posts/error")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(serverError()));
        }

        assertFalse(this.postServiceClient.getPostById("error").isPresent());
    }
}