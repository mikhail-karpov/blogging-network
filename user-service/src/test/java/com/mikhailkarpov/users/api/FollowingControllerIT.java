package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FollowingControllerIT extends AbstractControllerIT {

    @MockBean
    private FollowingEventPublisher eventPublisher;
    //todo test messaging

    @Test
    void testFollowingAndUnfollowingCase() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String userEmail = String.format("%s@example.com", username);
        String userPassword = "pa55word";

        String followerName = RandomStringUtils.randomAlphabetic(15);
        String followerEmail = String.format("%s@example.com", followerName);
        String followerPassword = "password";

        UserProfileDto user = registerUser(username, userEmail, userPassword);
        UserProfileDto follower = registerUser(followerName, followerEmail, followerPassword);

        //when
        loginAndFollowUser(followerName, followerPassword, user.getId());
        PagedResult<UserProfileDto> followers = getFollowers(username, userPassword, user.getId());
        PagedResult<UserProfileDto> followings = getFollowings(username, userPassword, follower.getId());

        //then
        assertThat(followers.getResult()).containsOnly(follower);
        assertThat(followings.getResult()).containsOnly(user);

        //and when
        loginAndUnfollowUser(followerName, followerPassword, user.getId());
        followers = getFollowers(followerName, followerPassword, user.getId());
        followings = getFollowings(username, userPassword, follower.getId());

        //then
        assertThat(followers.getResult()).isEmpty();
        assertThat(followings.getResult()).isEmpty();
    }

    private void loginAndFollowUser(String username, String password, String userId) {
        //when
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        ResponseEntity<Void> followingResponse =
                this.restTemplate.exchange("/users/{id}/followers", POST, new HttpEntity<>(headers), Void.class, userId);

        //then
        assertThat(followingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void loginAndUnfollowUser(String username, String password, String userId) {
        //when
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        ResponseEntity<Void> followingResponse =
                this.restTemplate.exchange("/users/{id}/followers", DELETE, new HttpEntity<>(headers), Void.class, userId);

        //then
        assertThat(followingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private PagedResult<UserProfileDto> getFollowers(String username, String password, String userId) {
        //given
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        ParameterizedTypeReference<PagedResult<UserProfileDto>> typeRef =
                new ParameterizedTypeReference<PagedResult<UserProfileDto>>() {
                };

        //when
        ResponseEntity<PagedResult<UserProfileDto>> response =
                this.restTemplate.exchange("/users/{id}/followers", GET, new HttpEntity<>(headers), typeRef, userId);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    private PagedResult<UserProfileDto> getFollowings(String username, String password, String userId) {
        //given
        HttpHeaders headers = loginAndBuildAuthorizationHeader(username, password);
        ParameterizedTypeReference<PagedResult<UserProfileDto>> typeRef =
                new ParameterizedTypeReference<PagedResult<UserProfileDto>>() {
                };

        //when
        ResponseEntity<PagedResult<UserProfileDto>> response =
                this.restTemplate.exchange("/users/{id}/followings", GET, new HttpEntity<>(headers), typeRef, userId);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }
}
