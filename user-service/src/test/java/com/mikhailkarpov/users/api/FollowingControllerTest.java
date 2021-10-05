package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserProfileDtoMapper;
import com.mikhailkarpov.users.service.FollowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class)
class FollowingControllerTest extends AbstractControllerTest {

    @MockBean
    private FollowingService followingService;

    @MockBean
    private UserProfileDtoMapper profileDtoMapper;

    private final List<UserProfile> profilesList = Arrays.asList(
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com"),
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com"),
            new UserProfile(UUID.randomUUID().toString(), "user" + UUID.randomUUID(), "user1@example.com")
    );

    private final Page<UserProfile> profilesPage =
            new PageImpl<>(profilesList, PageRequest.of(1, 3), 6);

    @BeforeEach
    void setDtoMapper() {
        for (UserProfile userProfile : profilesList) {
            UserProfileDto dto = new UserProfileDto(userProfile.getId(), userProfile.getUsername());
            when(profileDtoMapper.map(userProfile)).thenReturn(dto);
        }
    }

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