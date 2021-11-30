package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
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
    void shouldCreateUserAndAuthenticate() {
        //given
        UserRegistrationRequest registrationRequest = DtoUtils.createRandomRequest();

        //when
        UserProfileDto profile = this.userService.registerUser(registrationRequest);

        //then
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUsername()).isEqualTo(registrationRequest.getUsername());

        //and when
        UserAuthenticationRequest authenticationRequest =
                new UserAuthenticationRequest(registrationRequest.getUsername(), registrationRequest.getPassword());
        AccessTokenResponse tokenResponse = this.userService.authenticateUser(authenticationRequest);

        //then
        assertThat(tokenResponse.getToken()).isNotNull();
    }

    @Test
    void givenInvalidCredentials_whenAuthenticate_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphabetic(10);
        UserAuthenticationRequest request = new UserAuthenticationRequest(username, password);

        //then
        assertThatThrownBy(() -> this.userService.authenticateUser(request)).isInstanceOf(WebApplicationException.class);
    }

    @Test
    @SqlGroup(value = {
            @Sql(scripts = "/db_scripts/insert_users.sql"),
            @Sql(scripts = "/db_scripts/delete_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    void givenUser_whenFindById_thenFound() {
        //when
        Optional<UserProfileDto> foundProfile = this.userService.findUserById("1");

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
        Optional<UserProfileDto> profile = this.userService.findUserById(userId);

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
        Page<UserProfileDto> profiles = this.userService.findUsersByUsernameLike("Smith", pageRequest);

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
        UserRegistrationRequest request =
                new UserRegistrationRequest(username, String.format("%s@example.com", username), "pa55word");
        UserRegistrationRequest duplicateUsernameRequest =
                new UserRegistrationRequest(username, String.format("%s@fake.com", username), "password");

        //then
        this.userService.registerUser(request);
        assertThatThrownBy(() -> this.userService.registerUser(duplicateUsernameRequest))
                .isInstanceOf(WebApplicationException.class);

        assertThat(this.userService.findUsersByUsernameLike(username, PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }

    @Test
    void givenDuplicateEmail_whenCreateUser_thenException() {
        //given
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = String.format("%s@example.com", RandomStringUtils.randomAlphabetic(5));

        UserRegistrationRequest request =
                new UserRegistrationRequest(username, email, "pa55word");
        UserRegistrationRequest duplicateEmailRequest =
                new UserRegistrationRequest(username + "a", email, "password");

        //then
        this.userService.registerUser(request);

        assertThatThrownBy(() -> this.userService.registerUser(duplicateEmailRequest))
                .isInstanceOf(WebApplicationException.class);

        assertThat(this.userService.findUsersByUsernameLike(username, PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }
}