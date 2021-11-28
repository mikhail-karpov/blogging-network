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
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
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
        UserProfileDto profile = this.userService.create(request);

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
    @Sql(
            scripts = {"/db_scripts/insert_users.sql"})
    @Sql(
            scripts = {"/db_scripts/delete_users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUser_whenFindById_thenFound() {
        //when
        Optional<UserProfileDto> foundProfile = this.userService.findById("1");

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
        Optional<UserProfileDto> profile = this.userService.findById(userId);

        //then
        assertThat(profile).isEmpty();
    }

    @Test
    @Sql(
            scripts = {"/db_scripts/insert_users.sql"})
    @Sql(
            scripts = {"/db_scripts/delete_users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUsers_whenFindByUsername_thenFound() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "username"));
        Page<UserProfileDto> profiles = this.userService.findByUsernameLike("Smith", pageRequest);

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
        this.userService.create(new UserRegistrationRequest(username, "fakes@example.com", "pa55word"));

        //then
        assertThatThrownBy(() -> this.userService.create(
                new UserRegistrationRequest(username, "example@fake.com", "password")))
                .isInstanceOf(WebApplicationException.class);

        assertThat(this.userService.findByUsernameLike(username, PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }

    @Test
    void givenDuplicateEmail_whenCreateUser_thenException() {
        //given
        String email = String.format("%s@example.com", RandomStringUtils.randomAlphabetic(5));
        this.userService.create(new UserRegistrationRequest("Foo", email, "password"));

        //then
        assertThatThrownBy(() -> this.userService.create(
                new UserRegistrationRequest("Bar", email, "pa55word")))
                .isInstanceOf(WebApplicationException.class);

        assertThat(this.userService.findByUsernameLike("Foo", PageRequest.of(1, 5)).getTotalElements())
                .isEqualTo(1L);
    }
}