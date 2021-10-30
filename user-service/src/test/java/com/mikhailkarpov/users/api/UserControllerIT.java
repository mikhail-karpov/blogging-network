package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.UserProfileDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT extends AbstractControllerIT {

    @Test
    void givenUserRegistered_whenGetProfile_thenOk() {
        //given
        String username = RandomStringUtils.randomAlphabetic(15);
        String email = username + "@example.com";
        String password = "pass";

        //when
        UserProfileDto user = registerUser(username, email, password);
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        ResponseEntity<UserProfileDto> response =
                this.restTemplate.exchange("/users/{id}/profile", GET, entity, UserProfileDto.class, user.getId());

        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasNoNullFieldsOrProperties();
        assertThat(response.getBody().getUsername()).isEqualTo(username);
    }
}