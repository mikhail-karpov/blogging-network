package com.mikhailkarpov.users.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final UserProfileDto profile = new UserProfileDto(UUID.randomUUID().toString(), "username");

    @Test
    void testCreateUser() throws Exception {
        //given
        UserRegistrationRequest request =
                new UserRegistrationRequest("username", "user@example.com", "password");
        when(this.userService.registerUser(request)).thenReturn(profile);

        //when
        this.mockMvc.perform(post("/account/registration")
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/account/profile"))
                .andExpect(jsonPath("$.userId").value(this.profile.getId()))
                .andExpect(jsonPath("$.username").value("username"));

        verify(this.userService).registerUser(request);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidRequests")
    void testCreateUserValidation(UserRegistrationRequest request) throws Exception {
        //when
        this.mockMvc.perform(post("/account/registration")
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
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

        when(this.userService.authenticateUser(request)).thenReturn(response);

        //when
        this.mockMvc.perform(post("/account/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(token));

        verify(userService).authenticateUser(request);
    }

    @Test
    void givenJwt_whenGetProfile_thenOk() throws Exception {
        //given
        String userId = this.profile.getId();
        when(this.userService.findUserById(userId)).thenReturn(Optional.of(profile));

        //when
        this.mockMvc.perform(get("/account/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("username"));
    }

    @Test
    void givenNoJwt_whenGetProfile_thenUnauthorized() throws Exception {
        //when
        mockMvc.perform(get("/account/profile"))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(this.userService);
    }
}