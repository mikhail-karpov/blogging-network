package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    public static final String DEFAULT_USERNAME = "Username not available";

    @Override
    public Optional<UserProfileDto> findById(String userId) {

        UserProfileDto dto = new UserProfileDto(userId, DEFAULT_USERNAME);
        return Optional.of(dto);
    }
}
