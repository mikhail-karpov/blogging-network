package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerIT extends AbstractIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void whenRegisterUserAndLoginAndGetProfile_thenOk() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = String.format("%s@example.com", username);
        String password = "pa55word";

        //when
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, email, password);
        ResponseEntity<UserProfileDto> registrationResponse =
                this.restTemplate.postForEntity("/account/registration", registrationRequest, UserProfileDto.class);

        //then
        assertThat(registrationResponse.getStatusCodeValue()).isEqualTo(201);
        assertThat(registrationResponse.getBody()).isNotNull();
        assertThat(registrationResponse.getBody().getId()).isNotNull();
        assertThat(registrationResponse.getBody().getUsername()).isEqualTo(username);

        //and when
        UserAuthenticationRequest authenticationRequest = new UserAuthenticationRequest(username, password);
        ResponseEntity<AccessTokenResponse> authenticationResponse =
                this.restTemplate.postForEntity("/account/login", authenticationRequest, AccessTokenResponse.class);

        //then
        assertThat(authenticationResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(authenticationResponse.getBody()).isNotNull();
        assertThat(authenticationResponse.getBody().getToken()).isNotNull();

        //and when
        URI location = registrationResponse.getHeaders().getLocation();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + authenticationResponse.getBody().getToken());
        ResponseEntity<UserProfileDto> profileResponse =
                this.restTemplate.exchange(location, GET, new HttpEntity<>(headers), UserProfileDto.class);

        //then
        assertThat(profileResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(profileResponse.getBody()).isNotNull();
        assertThat(profileResponse.getBody().getId()).isNotNull();
        assertThat(profileResponse.getBody().getUsername()).isEqualTo(username);
    }
}
