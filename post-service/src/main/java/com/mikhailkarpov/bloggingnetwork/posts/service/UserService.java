package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;

public interface UserService {

    UserProfileDto getUserById(String userId);
}
