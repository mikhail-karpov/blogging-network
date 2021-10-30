package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerIT extends AbstractControllerIT {

    @Test
    void whenRegisterUserAndLoginAndGetProfile_thenOk() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = String.format("%s@example.com", username);
        String password = "pa55word";

        //when
        registerUser(username, email, password);

        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);

        ResponseEntity<UserProfileDto> profile =
                this.restTemplate.exchange("/account/profile", GET, new HttpEntity<>(headers), UserProfileDto.class);

        //then
        assertThat(profile.getStatusCodeValue()).isEqualTo(200);
        assertThat(profile.getBody()).isNotNull();
        assertThat(profile.getBody().getId()).isNotNull();
        assertThat(profile.getBody().getUsername()).isEqualTo(username);
    }
}
