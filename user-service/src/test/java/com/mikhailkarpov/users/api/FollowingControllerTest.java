package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.FollowingService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
@AutoConfigureJsonTesters
class FollowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowingService followingService;

    @Autowired
    private JacksonTester<PagedResult<UserProfileDto>> resultJacksonTester;

    private final List<UserProfileDto> profilesList = Arrays.asList(
            new UserProfileDto(UUID.randomUUID().toString(), "user" + UUID.randomUUID()),
            new UserProfileDto(UUID.randomUUID().toString(), "user" + UUID.randomUUID()),
            new UserProfileDto(UUID.randomUUID().toString(), "user" + UUID.randomUUID())
    );

    private final Page<UserProfileDto> profilesPage =
            new PageImpl<>(profilesList, PageRequest.of(1, 3), 6);

    @Test
    void givenJwt_whenFollow_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        String followerId = UUID.randomUUID().toString();

        //then
        this.mockMvc.perform(post("/users/{id}/followers", userId)
                        .with(jwt().jwt(jwt -> jwt.subject(followerId))))
                .andExpect(status().isOk());

        //then
        verify(this.followingService).addToFollowers(userId, followerId);
    }

    @Test
    void givenNoJwt_whenFollow_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(post("/users/{id}/followers", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(followingService);
    }

    @Test
    void givenJwt_whenUnfollow_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        String followerId = UUID.randomUUID().toString();

        //when
        this.mockMvc.perform(delete("/users/{id}/followers", userId)
                        .with(jwt().jwt(jwt -> jwt.subject(followerId))))
                .andExpect(status().isOk());

        //then
        verify(this.followingService).removeFromFollowers(userId, followerId);
    }

    @Test
    void givenNoJwt_whenUnfollow_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(delete("/users/{id}/followers", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(followingService);
    }

    @Test
    void givenAuth_whenGetFollowers_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(1, 3);
        when(this.followingService.findFollowers(userId, pageable)).thenReturn(profilesPage);

        //when
        MockHttpServletResponse response = this.mockMvc.perform(
                        get("/users/{id}/followers?page=1&size=3", userId)
                                .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(
                resultJacksonTester.write(new PagedResult<>(profilesPage)).getJson()
        );
    }

    @Test
    void givenNoAuth_whenGetFollowers_thenUnauthorized() throws Exception {
        //given
        mockMvc.perform(get("/users/{id}/followers", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(followingService);
    }

    @Test
    void givenAuth_whenGetFollowings_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(1, 3);
        when(followingService.findFollowing(userId, pageable)).thenReturn(profilesPage);

        //when
        MockHttpServletResponse response = this.mockMvc.perform(
                get("/users/{id}/following?page=1&size=3", userId)
                        .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(
                resultJacksonTester.write(new PagedResult<>(profilesPage)).getJson()
        );
    }

    @Test
    void givenNoAuth_whenGetFollowings_thenUnauthorized() throws Exception {
        //given
        mockMvc.perform(get("/users/{id}/followings", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(followingService);
    }
}