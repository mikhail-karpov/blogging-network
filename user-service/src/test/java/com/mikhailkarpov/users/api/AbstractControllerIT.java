package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractControllerIT extends AbstractIT {

    @Autowired
    TestRestTemplate restTemplate;

    protected final UserProfileDto registerUser(String username, String email, String password) {
        //given
        UserRegistrationRequest request = new UserRegistrationRequest(username, email, password);

        //when
        ResponseEntity<UserProfileDto> response =
                this.restTemplate.postForEntity("/account/registration", request, UserProfileDto.class);

        //then
        assertThat(response.getStatusCodeValue()).isEqualTo(201);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo(username);

        return response.getBody();
    }

    private final AccessTokenResponse login(String username, String password) {
        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);

        //when
        ResponseEntity<AccessTokenResponse> accessTokenResponse =
                this.restTemplate.postForEntity("/account/login", request, AccessTokenResponse.class);

        //then
        assertThat(accessTokenResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(accessTokenResponse.getBody()).isNotNull();
        assertThat(accessTokenResponse.getBody().getToken()).isNotNull();

        return accessTokenResponse.getBody();
    }

    protected final String obtainAccessToken(String username, String password) {

        return login(username, password).getToken();
    }

    protected HttpHeaders loginAndBuildAuthorizationHeader(String username, String password) {

        String accessToken = obtainAccessToken(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        return headers;
    }
}
