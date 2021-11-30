package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        String username = "DonaldTrump";
        UserProfileDto dto = new UserProfileDto(id, username);

        Mockito.when(this.userService.findUserById(id)).thenReturn(Optional.of(dto));

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
        Mockito.when(this.userService.findUserById(id)).thenReturn(Optional.empty());

        //when
        this.mockMvc.perform(get("/users/{id}/profile", id)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNoAuth_whenGetById_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(get("/users/{id}/profile", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(this.userService);
    }

    @Test
    void givenProfiles_whenSearchByUsername_thenOk() throws Exception {
        //given
        UserProfileDto user1 = new UserProfileDto("user1", "username1");
        UserProfileDto user2 = new UserProfileDto("user2", "username2");

        when(this.userService.findUsersByUsernameLike("username", PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2), PageRequest.of(1, 2), 4L));

        //when
        this.mockMvc.perform(get("/users/search?username={username}&page=1&size=2", "username")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(4))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.size()").value(2))
                .andExpect(jsonPath("$.result[0].userId").value("user1"))
                .andExpect(jsonPath("$.result[0].username").value("username1"))
                .andExpect(jsonPath("$.result[1].userId").value("user2"))
                .andExpect(jsonPath("$.result[1].username").value("username2"));
    }

    @Test
    void givenNoAuth_whenSearchByUsername_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(get("/users/search?username={username}", "username"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(this.userService);
    }
}