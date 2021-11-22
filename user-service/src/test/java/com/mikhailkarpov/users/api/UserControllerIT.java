package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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
        String password = "password";
        UserProfileDto user = registerUser(username, email, password);

        //when
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        ResponseEntity<UserProfileDto> response =
                this.restTemplate.exchange("/users/{id}/profile", GET, new HttpEntity<>(headers), UserProfileDto.class, user.getId());

        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasNoNullFieldsOrProperties();
        assertThat(response.getBody().getUsername()).isEqualTo(username);
    }

    @Test
    void givenUsersRegistered_whenSearchByUsername_thenOk() {
        //given
        for (int i = 0; i < 5; i++) {
            String username = "jamesbond" + i;
            registerUser(username, username + "@example.com", "password");
        }

        //when
        HttpHeaders headers = loginAndBuildAuthorizationHeader("jamesbond0", "password");
        ResponseEntity<PagedResult<UserProfileDto>> response =
                this.restTemplate.exchange("/users/search?username=JamesBond&page=1&size=3",
                        GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<PagedResult<UserProfileDto>>() {
                        });

        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasNoNullFieldsOrProperties();
        assertThat(response.getBody().getTotalResults()).isEqualTo(5L);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
        assertThat(response.getBody().getResult().size()).isEqualTo(2);
    }
}