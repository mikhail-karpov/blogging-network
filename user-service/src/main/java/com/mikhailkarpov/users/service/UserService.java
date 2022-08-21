package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.dto.AccessTokenDto;
import com.mikhailkarpov.users.dto.SignInRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.SignUpRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    Optional<UserProfileDto> findUserById(String id);

    Page<UserProfileDto> findUsersByUsernameLike(String username, Pageable pageable);

    UserProfileDto signUp(SignUpRequest request);

    AccessTokenDto signIn(SignInRequest request);
}
