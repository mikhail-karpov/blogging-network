package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import javax.ws.rs.WebApplicationException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceIT extends AbstractIT {

    @Autowired
    private UserService userService;

    @Test
    void givenRequest_whenCreateUser_thenCreated() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        UserProfileIntf profile = this.userService.create(request);

        //then
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUsername()).isEqualTo(request.getUsername());
    }

    @Test
    void givenCreatedUser_whenAuthenticate_thenTokenIsReturned() {
        //given
        UserRegistrationRequest createUserRequest = DtoUtils.createRandomRequest();
        this.userService.create(createUserRequest);

        //when
        UserAuthenticationRequest authenticationRequest =
                new UserAuthenticationRequest(createUserRequest.getUsername(), createUserRequest.getPassword());
        AccessTokenResponse tokenResponse = this.userService.authenticate(authenticationRequest);

        //then
        assertThat(tokenResponse.getToken()).isNotNull();
    }

    @Test
    void givenNoUser_whenAuthenticate_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphabetic(10);
        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);

        //then
        assertThatThrownBy(() -> this.userService.authenticate(request)).isInstanceOf(WebApplicationException.class);
    }

    @Test
    void givenCreatedUser_whenFindById_thenFound() {
        //given
        UserRegistrationRequest createUserRequest = DtoUtils.createRandomRequest();
        UserProfileIntf profile = this.userService.create(createUserRequest);

        //when
        Optional<UserProfileIntf> foundProfile = this.userService.findById(profile.getId());

        //then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getId()).isEqualTo(profile.getId());
        assertThat(foundProfile.get().getUsername()).isEqualTo(createUserRequest.getUsername());
    }

    @Test
    void givenNoUser_whenFindById_thenNotFound() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        Optional<UserProfileIntf> profile = this.userService.findById(userId);

        //then
        assertThat(profile).isEmpty();
    }

    @Test
    void givenCreatedUsers_whenFindByUsername_thenFound() {
        //given
        this.userService.create(
                new UserRegistrationRequest("johnsmith", "john@example.com", "password"));
        this.userService.create(
                new UserRegistrationRequest("adamsmith", "adam@example.com", "password"));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileIntf> profiles = this.userService.findByUsernameLike("Smith", pageRequest);

        //then
        assertThat(profiles.getTotalPages()).isEqualTo(1);
        assertThat(profiles.getTotalElements()).isEqualTo(2L);
        assertThat(profiles.getNumberOfElements()).isEqualTo(2);
    }

    @Test
    void givenDuplicateUsername_whenCreateUser_thenOnlyOneUserCreated() {
        //given
        String username = RandomStringUtils.randomAlphabetic(15);
        UserRegistrationRequest request =
                new UserRegistrationRequest(username, String.format("%s@example.com", username), "password");
        UserRegistrationRequest duplicateUsernameRequest =
                new UserRegistrationRequest(username, String.format("%s@fake.com", username), "pa55word");

        //when
        this.userService.create(request);

        //then
        assertThatThrownBy(() -> this.userService.create(duplicateUsernameRequest))
                .isInstanceOf(WebApplicationException.class);

        //and when
        Page<UserProfileIntf> profiles =
                this.userService.findByUsernameLike(username, PageRequest.of(1, 5));

        //then
        assertThat(profiles.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void givenDuplicateEmail_whenCreateUser_thenOnlyOneUserCreated() {
        //given
        String email = String.format("%s@example.com", RandomStringUtils.randomAlphabetic(15));
        String username1 = RandomStringUtils.randomAlphabetic(10);
        String username2 = username1 + "foo";

        UserRegistrationRequest request =
                new UserRegistrationRequest(username1, email, "password");
        UserRegistrationRequest duplicateEmailRequest =
                new UserRegistrationRequest(username2, email, "pa55word");

        //when
        this.userService.create(request);

        //then
        assertThatThrownBy(() -> this.userService.create(duplicateEmailRequest))
                .isInstanceOf(WebApplicationException.class);

        //and when
        Page<UserProfileIntf> profiles =
                this.userService.findByUsernameLike(username1, PageRequest.of(1, 5));

        //then
        assertThat(profiles.getTotalElements()).isEqualTo(1L);
    }
}