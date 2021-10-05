package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FollowingControllerIT extends AbstractIT {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ParameterizedTypeReference<PagedResult<UserProfileDto>> pagedProfilesRef
            = new ParameterizedTypeReference<PagedResult<UserProfileDto>>() {
    };

    private final UserRegistrationRequest createUserRequest = DtoUtils.createRandomRequest();
    private final UserRegistrationRequest createFollower1Request = DtoUtils.createRandomRequest();
    private final UserRegistrationRequest createFollower2Request = DtoUtils.createRandomRequest();

    private UserProfileDto createUser(UserRegistrationRequest request) {

        return restTemplate.postForObject("/users/registration", request, UserProfileDto.class);
    }

    private ResponseEntity<AccessTokenResponse> login(String username, String password) {
        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);
        return restTemplate.postForEntity("/users/login", request, AccessTokenResponse.class);
    }

    private HttpHeaders buildAuthHeader(String username, String password) {
        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);
        AccessTokenResponse accessTokenResponse =
                restTemplate.postForObject("/users/login", request, AccessTokenResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessTokenResponse.getToken());

        return headers;
    }

    private ResponseEntity<Object> follow(String followerUsername, String followerPassword, String userId) {
        HttpHeaders headers = buildAuthHeader(followerUsername, followerPassword);
        ResponseEntity<Object> followResponse =
                restTemplate.exchange("/users/{id}/followers", POST, new HttpEntity<>(headers), Object.class, userId);
        return followResponse;
    }

    private ResponseEntity<Object> unfollow(String followerUsername, String followerPassword, String userId) {
        HttpHeaders headers = buildAuthHeader(followerUsername, followerPassword);
        ResponseEntity<Object> unfollowResponse =
                restTemplate.exchange("/users/{id}/followers", DELETE, new HttpEntity<>(headers), Object.class, userId);
        return unfollowResponse;
    }

    private ResponseEntity<PagedResult<UserProfileDto>> getFollowers(String username, String password, String userId) {
        //given
        HttpHeaders headers = buildAuthHeader(username, password);

        //when
        ResponseEntity<PagedResult<UserProfileDto>> getFollowersResponse =
                restTemplate.exchange("/users/{id}/followers", GET, new HttpEntity<>(headers), pagedProfilesRef, userId);

        //then
        assertThat(getFollowersResponse.getStatusCode()).isEqualTo(OK);
        return getFollowersResponse;
    }

    private ResponseEntity<PagedResult<UserProfileDto>> getFollowings(String username, String password, String userId) {
        //given
        HttpHeaders headers = buildAuthHeader(username, password);

        //when
        ResponseEntity<PagedResult<UserProfileDto>> getFollowingsResponse =
                restTemplate.exchange("/users/{id}/followings", GET, new HttpEntity<>(headers), pagedProfilesRef, userId);

        //then
        assertThat(getFollowingsResponse.getStatusCode()).isEqualTo(OK);
        return getFollowingsResponse;
    }

    @Test
    void givenUsers_whenFollowAndGetFollowersAndGetFollowings_thenOk() {
        //given
        UserProfileDto user = createUser(createUserRequest);
        UserProfileDto follower1 = createUser(createFollower1Request);
        UserProfileDto follower2 = createUser(createFollower2Request);

        //when
        String username = createFollower1Request.getUsername();
        String password = createFollower1Request.getPassword();
        ResponseEntity<Object> followResponse = follow(username, password, user.getId());
        ResponseEntity<PagedResult<UserProfileDto>> followingsResponse = getFollowings(username, password, follower1.getId());

        //then
        assertThat(followResponse.getStatusCode()).isEqualTo(OK);
        assertThat(followingsResponse.getBody()).isNotNull();
        assertThat(followingsResponse.getBody().getTotalResults()).isEqualTo(1L);
        assertThat(followingsResponse.getBody().getResult()).contains(user);

        //and when
        username = createFollower2Request.getUsername();
        password = createFollower2Request.getPassword();
        followResponse = follow(username, password, user.getId());
        followingsResponse = getFollowings(username, password, follower2.getId());

        //then
        assertThat(followResponse.getStatusCode()).isEqualTo(OK);
        assertThat(followingsResponse.getBody()).isNotNull();
        assertThat(followingsResponse.getBody().getTotalResults()).isEqualTo(1L);
        assertThat(followingsResponse.getBody().getResult()).contains(user);

        //and when
        username = createUserRequest.getUsername();
        password = createUserRequest.getPassword();
        ResponseEntity<PagedResult<UserProfileDto>> followers = getFollowers(username, password, user.getId());

        //then
        assertThat(followers.getBody()).isNotNull();
        assertThat(followers.getBody().getTotalResults()).isEqualTo(2L);
        assertThat(followers.getBody().getResult()).contains(follower1, follower2);
    }

    @Test
    void givenNoUser_whenFollow_thenNotFound() {
        //given
        String userId = UUID.randomUUID().toString();
        UserProfileDto follower = createUser(createUserRequest);

        //when
        String username = createUserRequest.getUsername();
        String password = createUserRequest.getPassword();
        ResponseEntity<Object> followResponse = follow(username, password, userId);

        //then
        assertThat(followResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void givenFollowing_whenUnfollowAndGetFollowings_thenEmpty() {
        //given
        UserProfileDto user = createUser(createUserRequest);
        UserProfileDto follower = createUser(createFollower1Request);
        String username = createFollower1Request.getUsername();
        String password = createFollower2Request.getPassword();
        follow(username, password, user.getId());

        //when
        ResponseEntity<Object> unfollowResponse = unfollow(username, password, user.getId());

        //then
        assertThat(unfollowResponse.getStatusCode()).isEqualTo(OK);

        //and when
        ResponseEntity<PagedResult<UserProfileDto>> followingsResponse = getFollowings(username, password, follower.getId());

        //then
        assertThat(followingsResponse.getBody().getTotalResults()).isEqualTo(0L);
    }

    @Test
    void givenNoUser_whenUnFollow_thenNotFound() {
        //given
        String userId = UUID.randomUUID().toString();
        UserProfileDto follower = createUser(createUserRequest);

        //when
        String username = createUserRequest.getUsername();
        String password = createUserRequest.getPassword();
        ResponseEntity<Object> unfollowResponse = unfollow(username, password, userId);

        //then
        assertThat(unfollowResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }
}