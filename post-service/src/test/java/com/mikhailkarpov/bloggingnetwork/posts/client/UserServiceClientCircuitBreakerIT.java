package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.config.MockUserServiceConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = MockUserServiceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceClientCircuitBreakerIT extends AbstractIT {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private List<WireMockServer> servers;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void circuitBreakerShouldExists() {
        CircuitBreaker circuitBreaker = this.circuitBreakerRegistry.circuitBreaker("user-service");
        assertThat(circuitBreaker).isNotNull();
    }

    @Test
    void givenOkResponse_whenGetUserById_thenPresent() {
        //given
        this.servers.forEach(service -> service.stubFor(get(urlMatching("/users/abc/profile"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody("{\"userId\":\"abc\", \"username\":\"abc-username\"}"))));

        //when
        Optional<UserProfileDto> user = this.userServiceClient.findById("abc");

        //then
        assertThat(user).isPresent();
        assertThat(user.get().getUserId()).isEqualTo("abc");
        assertThat(user.get().getUsername()).isEqualTo("abc-username");
    }

    @Test
    void givenNotFoundResponse_whenGetUserById_thenEmpty() {
        //given
        this.servers.forEach(service -> service.stubFor(get(urlMatching("/users/not-found/profile"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse().withStatus(404))));

        //when
        Optional<UserProfileDto> user = this.userServiceClient.findById("not-found");

        //then
        assertThat(user).isEmpty();
    }

    @Test
    void givenServerError_whenGetUserById_thenFallback() {
        //given
        this.servers.forEach(service -> service.stubFor(get("/users/error/profile")
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse().withStatus(500))));

        //when
        Optional<UserProfileDto> user = this.userServiceClient.findById("error");

        //then
        assertThat(user).isEmpty();
    }
}