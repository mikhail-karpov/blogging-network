package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfileIntf;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    AccessTokenResponse authenticate(UserAuthenticationRequest request);

    UserProfileIntf create(UserRegistrationRequest request);

    Optional<UserProfileIntf> findById(String id);

    Page<UserProfileIntf> findByUsernameLike(String username, Pageable pageable);
}
