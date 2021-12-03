package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Optional<UserProfileDto> findById(String userId) {

        return Optional.empty();
    }
}
