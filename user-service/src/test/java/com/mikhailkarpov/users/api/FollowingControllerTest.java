package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserProfileDtoMapper;
import com.mikhailkarpov.users.service.FollowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class)
class FollowingControllerTest {

    @MockBean
    private FollowingService followingService;

    @MockBean
    private UserProfileDtoMapper profileDtoMapper;

    @Autowired
    private MockMvc mockMvc;

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
    void addToFollowers() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        String followerId = UUID.randomUUID().toString();

        //then
        mockMvc.perform(post("/users/{id}/followers?followerId={followerId}", userId, followerId))
                .andExpect(status().isOk());
    }

    @Test
    void removeFromFollowers() throws Exception {
        //given
        String userId = UUID.randomUUID().toString();
        String followerId = UUID.randomUUID().toString();

        //when
        mockMvc.perform(delete("/users/{id}/followers?followerId={followerId}", userId, followerId))
                .andExpect(status().isOk());
    }

    @Test
    void getFollowers() throws Exception {
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
    void getFollowings() throws Exception {
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
}