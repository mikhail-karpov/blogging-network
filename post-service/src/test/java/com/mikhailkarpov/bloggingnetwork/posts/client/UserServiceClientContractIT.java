package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov:user-service:+:stubs",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@LoadBalancerClient
@TestPropertySource(properties = "feign.circuitbreaker.enabled=false")
class UserServiceClientContractIT extends AbstractIT {

    @Autowired
    private UserServiceClient userServiceClient;

    @Test
    void givenStub_whenFindById_thenPresent() {
        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("1");

        //then
        assertThat(profile).isPresent();
        assertThat(profile.get()).hasNoNullFieldsOrProperties();
    }

    @Test
    void givenStub_whenFindById_thenEmpty() {
        //when
        Optional<UserProfileDto> profile = this.userServiceClient.findById("0");

        //then
        assertThat(profile).isEmpty();
    }
}