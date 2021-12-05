package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.client.UserServiceClient;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserServiceClient userServiceClient;

    @Override
    @Cacheable("userCache")
    public UserProfileDto getUserById(String userId) {

        return this.userServiceClient.findById(userId).orElseThrow(() -> {
            String message = String.format("User with id=%s not found", userId);
            return new ResourceNotFoundException(message);
        });
    }
}
