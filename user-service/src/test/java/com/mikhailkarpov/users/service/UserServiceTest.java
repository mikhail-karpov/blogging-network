package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.AbstractIT;
import com.mikhailkarpov.users.dto.AccessTokenDto;
import com.mikhailkarpov.users.dto.SignInRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.SignUpRequest;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceTest extends AbstractIT {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserAndAuthenticate() {
        //given
        SignUpRequest registrationRequest = DtoUtils.createRandomRequest();

        //when
        UserProfileDto profile = userService.signUp(registrationRequest);

        //then
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUsername()).isEqualTo(registrationRequest.getUsername());

        //and when
        SignInRequest authenticationRequest =
                new SignInRequest(registrationRequest.getUsername(), registrationRequest.getPassword());
        AccessTokenDto accessToken = userService.signIn(authenticationRequest);

        //then
        assertThat(accessToken).isNotNull();
    }

    @Test
    void givenInvalidCredentials_whenAuthenticate_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphabetic(10);
        SignInRequest request = new SignInRequest(username, password.toCharArray());

        //then
        assertThatThrownBy(() -> userService.signIn(request)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @SqlGroup(value = {
            @Sql(scripts = "/db_scripts/insert_users.sql"),
            @Sql(scripts = "/db_scripts/delete_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    void givenUser_whenFindById_thenFound() {
        //when
        Optional<UserProfileDto> foundProfile = userService.findUserById("1");

        //then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getId()).isEqualTo("1");
        assertThat(foundProfile.get().getUsername()).isEqualTo("johnsmith");
    }

    @Test
    void givenNoUser_whenFindById_thenNotFound() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        Optional<UserProfileDto> profile = userService.findUserById(userId);

        //then
        assertThat(profile).isEmpty();
    }

    @Test
    @SqlGroup(value = {
            @Sql(scripts = "/db_scripts/insert_users.sql"),
            @Sql(scripts = "/db_scripts/delete_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    void givenUsers_whenFindByUsername_thenFound() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "username"));
        Page<UserProfileDto> profiles = userService.findUsersByUsernameLike("Smith", pageRequest);

        //then
        assertThat(profiles.getTotalPages()).isEqualTo(1);
        assertThat(profiles.getTotalElements()).isEqualTo(2L);
        assertThat(profiles.getNumberOfElements()).isEqualTo(2);
        assertThat(profiles.getContent().get(0).getUsername()).isEqualTo("adamsmith");
        assertThat(profiles.getContent().get(1).getUsername()).isEqualTo("johnsmith");
    }

    @Test
    void givenDuplicateUsername_whenCreateUser_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        SignUpRequest request =
                new SignUpRequest(username, String.format("%s@example.com", username), "pa55word".toCharArray());
        SignUpRequest duplicateUsernameRequest =
                new SignUpRequest(username, String.format("%s@fake.com", username), "password".toCharArray());

        //then
        userService.signUp(request);
        assertThatThrownBy(() -> userService.signUp(duplicateUsernameRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        assertThat(userService.findUsersByUsernameLike(username, PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }

    @Test
    void givenDuplicateEmail_whenCreateUser_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = String.format("%s@example.com", RandomStringUtils.randomAlphabetic(5));

        SignUpRequest request =
                new SignUpRequest(username, email, "pa55word".toCharArray());
        SignUpRequest duplicateEmailRequest =
                new SignUpRequest(username + "a", email, "password".toCharArray());

        //then
        userService.signUp(request);

        assertThatThrownBy(() -> userService.signUp(duplicateEmailRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        assertThat(userService.findUsersByUsernameLike(username, PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }
}