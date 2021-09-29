package com.mikhailkarpov.users.dto;

import com.mikhailkarpov.users.domain.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileDtoMapperTest {

    private UserProfileDtoMapper dtoMapper = new UserProfileDtoMapper();

    @Test
    void map() {
        //given
        String id = UUID.randomUUID().toString();
        String username = "username";
        String email = "username@example.com";
        UserProfile profile = new UserProfile(id, username, email);

        //when
        UserProfileDto profileDto = dtoMapper.map(profile);

        //then
        assertEquals(id, profileDto.getId());
        assertEquals(username, profileDto.getUsername());
    }
}