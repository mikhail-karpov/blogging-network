package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import org.keycloak.representations.AccessTokenResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    AccessTokenResponse authenticate(UserAuthenticationRequest request);

    UserProfile create(UserRegistrationRequest request);

    Iterable<UserProfile> findByIdIn(List<String> idList);

    Optional<UserProfile> findById(String id);

}
