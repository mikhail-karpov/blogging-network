package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT extends AbstractIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void givenRequest_whenPostThenGetUserById_thenCreatedAndFound() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        ResponseEntity<UserProfileDto> createdResponseEntity =
                restTemplate.postForEntity("/users", request, UserProfileDto.class);
        URI location = createdResponseEntity.getHeaders().getLocation();
        UserProfileDto foundProfile = restTemplate.getForObject(location, UserProfileDto.class);

        //then
        assertEquals(HttpStatus.CREATED, createdResponseEntity.getStatusCode());
        assertNotNull(foundProfile);
        assertNotNull(foundProfile.getId());
        assertEquals(request.getUsername(), foundProfile.getUsername());
    }

    @Test
    void givenDuplicateRequest_whenPostUser_thenConflict() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        restTemplate.postForEntity("/users", request, UserProfileDto.class);
        ResponseEntity<UserProfileDto> responseEntity =
                restTemplate.postForEntity("/users", request, UserProfileDto.class);

        //then
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    void givenNoUserExists_whenGetById_thenNotFound() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        ResponseEntity<UserProfileDto> responseEntity =
                restTemplate.getForEntity("/users/{id}", UserProfileDto.class, userId);

        //then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}
