package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
public class UserControllerIT extends AbstractControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void givenRequest_whenPostAndGetUserById_thenCreatedAndFound() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        ResponseEntity<UserProfileDto> postResponse =
                restTemplate.postForEntity("/users/registration", request, UserProfileDto.class);
        URI location = postResponse.getHeaders().getLocation();

        //then
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getHeaders().getLocation());

        //and when
        HttpHeaders headers = buildAuthHeader(request.getUsername(), request.getPassword());
        ResponseEntity<UserProfileDto> getResponse =
                restTemplate.exchange(location, GET, new HttpEntity<>(headers), UserProfileDto.class);

        //then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertNotNull(getResponse.getBody().getId());
        assertEquals(request.getUsername(), getResponse.getBody().getUsername());
    }

    @Test
    void givenDuplicateRequest_whenPostUser_thenConflict() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        ResponseEntity<UserProfileDto> registerUserResponse = null;
        for (int i = 0; i < 2; i++) {
            registerUserResponse = restTemplate.postForEntity("/users/registration", request, UserProfileDto.class);
        }

        //then
        assertEquals(HttpStatus.CONFLICT, registerUserResponse.getStatusCode());
    }

    @Test
    void givenNoUserExists_whenGetById_thenNotFound() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();
        restTemplate.postForEntity("/users/registration", request, Object.class);
        HttpHeaders headers = buildAuthHeader(request.getUsername(), request.getPassword());
        String userId = UUID.randomUUID().toString();

        //when
        ResponseEntity<UserProfileDto> responseEntity =
                restTemplate.exchange("/users/{id}", GET, new HttpEntity<>(headers), UserProfileDto.class, userId);

        //then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
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
