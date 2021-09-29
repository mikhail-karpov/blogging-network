package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.UserService;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FollowingControllerIT extends AbstractIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private final ParameterizedTypeReference<PagedResult<UserProfileDto>> profileRef
            = new ParameterizedTypeReference<PagedResult<UserProfileDto>>() {
    };


    private ResponseEntity<Object> sendFollowRequest(UserProfile follower, UserProfile user) {
        String postUri = String.format("/users/%s/followers?followerId=%s", user.getId(), follower.getId());
        return restTemplate.exchange(postUri, HttpMethod.POST, null, Object.class);
    }

    private ResponseEntity<PagedResult<UserProfileDto>> getFollowers(UserProfile user, int page, int size) {
        String getUri = String.format("/users/%s/followers?page=%d&size=%d", user.getId(), page, size);
        return restTemplate.exchange(getUri, HttpMethod.GET, null, profileRef);
    }

    private ResponseEntity<PagedResult<UserProfileDto>> getFollowings(UserProfile follower, int page, int size) {
        String getUri = String.format("/users/%s/followings?page=%d&size=%d", follower.getId(), page, size);
        return restTemplate.exchange(getUri, HttpMethod.GET, null, profileRef);
    }

    private ResponseEntity<Object> sendUnfollowRequest(UserProfile follower, UserProfile user) {
        String deleteUrl = String.format("/users/%s/followers?followerId=%s", user.getId(), follower.getId());
        return restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, Object.class);
    }

    @Test
    void testAddToFollowers_thenFindFollowers() {
        //given
        UserProfile user = userService.create(DtoUtils.createRandomRequest());

        for (int i = 0; i < 5; i++) {
            UserProfile follower = userService.create(DtoUtils.createRandomRequest());
            ResponseEntity<Object> followResponse = sendFollowRequest(follower, user);

            assertEquals(OK, followResponse.getStatusCode());
        }

        //when
        ResponseEntity<PagedResult<UserProfileDto>> getFollowersResponse = getFollowers(user, 1, 3);

        //then
        assertEquals(OK, getFollowersResponse.getStatusCode());
        assertNotNull(getFollowersResponse.getBody());
        assertEquals(2, getFollowersResponse.getBody().getResult().size());
        assertEquals(1, getFollowersResponse.getBody().getPage());
        assertEquals(2, getFollowersResponse.getBody().getTotalPages());
        assertEquals(5L, getFollowersResponse.getBody().getTotalResults());
    }

    @Test
    void testAddToFollowers_thenFindFollowings() {
        //given
        UserProfile follower = userService.create(DtoUtils.createRandomRequest());

        for (int i = 0; i < 5; i++) {
            UserProfile user = userService.create(DtoUtils.createRandomRequest());
            ResponseEntity<Object> addFollowerResponse = sendFollowRequest(follower, user);

            assertEquals(OK, addFollowerResponse.getStatusCode());
        }

        //when
        ResponseEntity<PagedResult<UserProfileDto>> getFollowingsResponse = getFollowings(follower, 1, 3);

        //then
        assertEquals(OK, getFollowingsResponse.getStatusCode());
        assertNotNull(getFollowingsResponse.getBody());
        assertEquals(2, getFollowingsResponse.getBody().getResult().size());
        assertEquals(1, getFollowingsResponse.getBody().getPage());
        assertEquals(2, getFollowingsResponse.getBody().getTotalPages());
        assertEquals(5L, getFollowingsResponse.getBody().getTotalResults());
    }

    @Test
    void testUnfollow() {
        //given
        UserProfile follower = userService.create(DtoUtils.createRandomRequest());
        UserProfile user = userService.create(DtoUtils.createRandomRequest());

        ResponseEntity<Object> addFollowerResponse = sendFollowRequest(follower, user);
        assertEquals(OK, addFollowerResponse.getStatusCode());

        ResponseEntity<Object> unfollowResponse = sendUnfollowRequest(follower, user);
        assertEquals(OK, unfollowResponse.getStatusCode());

        ResponseEntity<PagedResult<UserProfileDto>> getFollowingsResponse = getFollowings(follower, 0, 10);
        assertEquals(OK, getFollowingsResponse.getStatusCode());
        assertNotNull(getFollowingsResponse.getBody());
        assertTrue(getFollowingsResponse.getBody().getResult().isEmpty());
    }
}