package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.config.MockUserServiceConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {MockUserServiceConfig.class})
class UserServiceClientIT extends AbstractIT {

    @Autowired
    private List<WireMockServer> servers;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCircuitBreaker() {
        this.registry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
    }

    @Test
    void givenOkResponse_whenFindById_thenPresent() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(get("/users/abc/profile")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(okJson("{\"userId\":\"abc\", \"username\":\"abc-username\"}")));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("abc");

        //then
        assertThat(profile).isPresent();
        assertThat(profile.get().getUserId()).isEqualTo("abc");
        assertThat(profile.get().getUsername()).isEqualTo("abc-username");
    }

    @Test
    void givenNotFoundResponse_whenFindById_thenEmpty() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(get("/users/not-found/profile")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(WireMock.aResponse().withStatus(404)));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("not-found");

        //then
        assertThat(profile).isEmpty();
    }

    @Test
    void givenServerError_whenFindById_thenPresent() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(get("/users/error/profile")
                    .withHeader("Authorization", matching("Bearer .*"))
                    .willReturn(serverError()));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("error");

        //then
        assertThat(profile).isPresent();
        assertThat(profile.get().getUserId()).isEqualTo("error");
        assertThat(profile.get().getUsername()).isEqualTo(UserServiceClientFallback.DEFAULT_USERNAME);
    }
}