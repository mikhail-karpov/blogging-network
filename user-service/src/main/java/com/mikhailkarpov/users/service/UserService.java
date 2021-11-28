package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    AccessTokenResponse authenticate(UserAuthenticationRequest request);

    UserProfileDto create(UserRegistrationRequest request);

    Optional<UserProfileDto> findById(String id);

    Page<UserProfileDto> findByUsernameLike(String username, Pageable pageable);
}
