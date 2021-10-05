package com.mikhailkarpov.users.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserProfileDtoMapper;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest extends AbstractControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private UserProfileDtoMapper profileDtoMapper;

    private final UserRegistrationRequest request =
            new UserRegistrationRequest("username", "user@example.com", "password");

    private final UserProfile profile =
            new UserProfile(UUID.randomUUID().toString(), "username", "user@example.com");

    @BeforeEach
    void setProfileDtoMapper() {

        when(profileDtoMapper.map(profile)).thenReturn(new UserProfileDto(profile.getId(), profile.getUsername()));
    }

    @Test
    void testCreateUser() throws Exception {
        //given
        when(userService.create(request)).thenReturn(profile);

        //when
        mockMvc.perform(post("/users/registration")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/users/" + profile.getId()))
                .andExpect(jsonPath("$.userId").value(profile.getId()))
                .andExpect(jsonPath("$.username").value(request.getUsername()));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void testCreateUserValidation(UserRegistrationRequest request) throws Exception {
        //when
        mockMvc.perform(post("/users/registration")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> getInvalidRequests() {
        return Stream.of(
                Arguments.of(new UserRegistrationRequest(null, null, null)),
                Arguments.of(new UserRegistrationRequest("", "", "")),
                Arguments.of(new UserRegistrationRequest("username", "user@example.com", "")),
                Arguments.of(new UserRegistrationRequest("username", "user", "password"))
        );
    }

    @Test
    void givenAuthenticationRequest_whenLogin_thenOk() throws Exception {
        //given
        UserAuthenticationRequest request = new UserAuthenticationRequest("username", "password");
        String token = UUID.randomUUID().toString();
        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken(token);

        when(userService.authenticate(request)).thenReturn(response);

        //when
        mockMvc.perform(post("/users/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(token));

        verify(userService).authenticate(request);
    }

    @Test
    @WithMockUser
    void givenUser_whenGetUserById_thenOk() throws Exception {
        //given
        String userId = profile.getId();
        when(userService.findById(userId)).thenReturn(Optional.of(profile));

        //when
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value(profile.getUsername()));
    }

    @Test
    @WithMockUser
    void givenNoUser_whenGetUserById_thenNotFound() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        //when
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNoAuth_whenGetUserById_thenUnauthorized() throws Exception {
        //given
        String userId = profile.getId();
        when(userService.findById(userId)).thenReturn(Optional.of(profile));

        //when
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }
}