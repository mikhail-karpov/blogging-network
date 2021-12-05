package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.CacheConfig;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class UserServiceCacheableImpl implements UserService {

    private final UserService delegate;

    @Override
    public AccessTokenResponse authenticateUser(UserAuthenticationRequest request) {
        return this.delegate.authenticateUser(request);
    }

    @Override
    @CachePut(value = CacheConfig.USERS_CACHE, key = "#result.id")
    public UserProfileDto registerUser(UserRegistrationRequest request) {
        return this.delegate.registerUser(request);
    }

    @Override
    @Cacheable(value = CacheConfig.USERS_CACHE, unless = "#result == null")
    public Optional<UserProfileDto> findUserById(String id) {
        return this.delegate.findUserById(id);
    }

    @Override
    public Page<UserProfileDto> findUsersByUsernameLike(String username, Pageable pageable) {
        return this.delegate.findUsersByUsernameLike(username, pageable);
    }
}
