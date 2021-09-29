package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserProfile create(UserRegistrationRequest request);

    Iterable<UserProfile> findByIdIn(List<String> idList);

    Optional<UserProfile> findById(String id);

}
