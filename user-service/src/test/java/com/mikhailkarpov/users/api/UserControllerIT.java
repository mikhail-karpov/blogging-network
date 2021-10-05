package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT extends AbstractIT {

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<UserProfileDto> registerUser(UserRegistrationRequest request) {
        return restTemplate.postForEntity("/users/registration", request, UserProfileDto.class);
    }

    private ResponseEntity<AccessTokenResponse> login(String username, String password) {

        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);
        return restTemplate.postForEntity("/users/login", request, AccessTokenResponse.class);
    }

    private ResponseEntity<UserProfileDto> getUserById(String accessToken, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange("/users/{id}", GET, new HttpEntity<>(headers), UserProfileDto.class, userId);
    }

    @Test
    void givenRequest_whenRegisterUser_thenCreated() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        ResponseEntity<UserProfileDto> registerUserResponse = registerUser(request);
        URI location = registerUserResponse.getHeaders().getLocation();
        UserProfileDto userProfile = registerUserResponse.getBody();

        //then
        assertEquals(HttpStatus.CREATED, registerUserResponse.getStatusCode());
        assertNotNull(location);
        assertEquals("http://localhost:" + port + "/users/" + userProfile.getId(), location.toString());

        assertNotNull(userProfile);
        assertNotNull(userProfile.getId());
        assertEquals(request.getUsername(), userProfile.getUsername());
    }

    @Test
    void givenDuplicateRequest_whenPostUser_thenConflict() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        ResponseEntity<UserProfileDto> registerUserResponse = null;
        for (int i = 0; i < 2; i++) {
            registerUserResponse = registerUser(request);
        }

        //then
        assertEquals(HttpStatus.CONFLICT, registerUserResponse.getStatusCode());
    }

    @Test
    void givenUserRegistered_whenLogin_thenOk() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();
        registerUser(request);

        //when
        String username = request.getUsername();
        String password = request.getPassword();
        ResponseEntity<AccessTokenResponse> accessToken = login(username, password);
        AccessTokenResponse body = accessToken.getBody();

        //then
        assertEquals(HttpStatus.OK, accessToken.getStatusCode());
        assertNotNull(body);
        assertNotNull(body.getToken());
        assertNotNull(body.getTokenType());
        assertNotNull(body.getExpiresIn());
        assertNotNull(body.getRefreshToken());
        assertNotNull(body.getRefreshExpiresIn());
        assertNotNull(body.getScope());
    }

    @Test
    void givenNoUserRegistered_whenLogin_thenUnauthorized() {
        //given
        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        ResponseEntity<AccessTokenResponse> accessToken = login(username, password);

        //then
        assertEquals(HttpStatus.UNAUTHORIZED, accessToken.getStatusCode());
    }

    @Test
    void givenNoUserExists_whenGetById_thenNotFound() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();
        registerUser(request);

        //when
        String token = login(request.getUsername(), request.getPassword()).getBody().getToken();
        String userId = UUID.randomUUID().toString();
        ResponseEntity<UserProfileDto> userById = getUserById(token, userId);

        //then
        assertEquals(HttpStatus.NOT_FOUND, userById.getStatusCode());
    }

    @Test
    void givenNoAuth_whenGetById_thenUnauthorized() {
        //when
        ResponseEntity<UserProfileDto> response =
                restTemplate.getForEntity("/users/{id}", UserProfileDto.class, UUID.randomUUID().toString());

        //then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
