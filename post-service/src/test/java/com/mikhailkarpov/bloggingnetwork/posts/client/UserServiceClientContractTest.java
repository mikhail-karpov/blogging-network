package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.mikhailkarpov.bloggingnetwork.posts.config.TestSecurityConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableAutoConfiguration(exclude = RabbitAutoConfiguration.class)
@ContextConfiguration(classes = TestSecurityConfig.class)
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov:user-service:+:stubs",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class UserServiceClientContractTest {

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
}