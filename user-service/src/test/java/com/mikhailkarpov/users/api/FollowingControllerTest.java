package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.service.FollowingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class)
class FollowingControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowingService followingService;

    private final List<UserProfile> profilesList = Arrays.asList(
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com"),
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com"),
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com")
    );

    private final Page<UserProfile> profilesPage =
            new PageImpl<>(profilesList, PageRequest.of(1, 3), 6);

    @Test
    void givenJwt_whenFollow_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        String followerId = UUID.randomUUID().toString();

        //then
        mockMvc.perform(post("/users/{id}/followers", userId)
                        .with(jwt().jwt(jwt -> jwt.subject(followerId))))
                .andExpect(status().isOk());

        //then
        verify(followingService).addToFollowers(userId, followerId);
    }

    @Test
    void givenNoJwt_whenFollow_thenUnauthorized() throws Exception {
        //when
        mockMvc.perform(post("/users/{id}/followers", UUID.randomUUID().toString()))
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
        mockMvc.perform(delete("/users/{id}/followers", userId)
                        .with(jwt().jwt(jwt -> jwt.subject(followerId))))
                .andExpect(status().isOk());

        //then
        verify(followingService).removeFromFollowers(userId, followerId);
    }

    @Test
    void givenNoJwt_whenUnfollow_thenUnauthorized() throws Exception {
        //when
        mockMvc.perform(delete("/users/{id}/followers", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());

        //then
        verifyNoInteractions(followingService);
    }

    @Test
    @WithMockUser
    void givenAuth_whenGetFollowers_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(1, 3);
        when(followingService.findFollowers(userId, pageable)).thenReturn(profilesPage);

        //when
        mockMvc.perform(get("/users/{id}/followers?page=1&size=3", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(6))
                .andExpect(jsonPath("$.result.size()").value(3));

        //then
        verify(followingService).findFollowers(userId, pageable);
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
    @WithMockUser
    void givenAuth_whenGetFollowings_thenOk() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(1, 3);
        when(followingService.findFollowings(userId, pageable)).thenReturn(profilesPage);

        //when
        mockMvc.perform(get("/users/{id}/followings?page=1&size=3", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(6))
                .andExpect(jsonPath("$.result.size()").value(3));

        //then
        verify(followingService).findFollowings(userId, pageable);
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