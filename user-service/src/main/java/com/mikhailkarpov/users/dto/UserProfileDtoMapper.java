package com.mikhailkarpov.users.dto;

import com.mikhailkarpov.users.domain.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileDtoMapper {

    public UserProfileDto map(UserProfile userProfile) {
        return new UserProfileDto(userProfile.getId(), userProfile.getUsername());
    }
}
