package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureJsonTesters
@ContextConfiguration(classes = SecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private JacksonTester<UserProfileDto> dtoJacksonTester;

    @Test
    void givenUserFound_whenGetById_thenOk() throws Exception {
        //given
        String id = UUID.randomUUID().toString();
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = username + "@gmail.com";
        UserProfile profile = new UserProfile(id, username, email);
        UserProfileDto dto = new UserProfileDto(id, username);

        Mockito.when(this.userService.findById(id)).thenReturn(Optional.of(profile));

        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/users/{id}/profile", id)
                        .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(this.dtoJacksonTester.write(dto).getJson());
    }

    @Test
    void givenUserNotFound_whenGetById_thenNotFound() throws Exception {
        //given
        String id = UUID.randomUUID().toString();
        Mockito.when(this.userService.findById(id)).thenReturn(Optional.empty());

        //when
        this.mockMvc.perform(get("/users/{id}/profile", id)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNoAuth_whenGetById_thenUnauthorized() throws Exception {
        //given
        String id = UUID.randomUUID().toString();
        Mockito.when(this.userService.findById(id)).thenReturn(Optional.empty());

        //when
        this.mockMvc.perform(get("/users/{id}/profile", id))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(this.userService);
    }
}