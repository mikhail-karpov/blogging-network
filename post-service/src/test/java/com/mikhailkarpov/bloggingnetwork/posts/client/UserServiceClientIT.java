package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.config.MockUserServiceConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {MockUserServiceConfig.class})
class UserServiceClientIT extends AbstractIT {

    @Autowired
    private List<WireMockServer> servers;

    @Autowired
    private UserServiceClient userServiceClient;

    @AfterEach
    void tearDown() {
        for (WireMockServer server : this.servers) {
            server.resetAll();
        }
    }

    @Test
    void givenOkResponse_whenFindById_thenPresent() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(WireMock.get("/users/abc-user/profile")
                    .withHeader("Authorization", WireMock.matching("Bearer .*"))
                    .willReturn(WireMock.okJson("{\"userId\":\"abc-user\", \"username\":\"abc-username\"}")));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("abc-user");

        //then
        assertThat(profile).isPresent();
        assertThat(profile.get().getUserId()).isEqualTo("abc-user");
        assertThat(profile.get().getUsername()).isEqualTo("abc-username");
    }

    @Test
    void givenNotFoundResponse_whenFindById_thenPresent() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(WireMock.get("/users/not-found/profile")
                    .withHeader("Authorization", WireMock.matching("Bearer .*"))
                    .willReturn(WireMock.notFound()));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("not-found");

        //then
        assertThat(profile).isEmpty();
    }

    @Test
    void givenServerError_whenFindById_thenEmpty() {
        //given
        for (WireMockServer server : this.servers) {
            server.stubFor(WireMock.get("/users/error/profile")
                    .withHeader("Authorization", WireMock.matching("Bearer .*"))
                    .willReturn(WireMock.serverError()));
        }

        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("error");

        //then
        assertThat(profile).isPresent();
        assertThat(profile.get().getUserId()).isEqualTo("error");
        assertThat(profile.get().getUsername()).isEqualTo(UserServiceClientFallback.DEFAULT_USERNAME);
    }
}