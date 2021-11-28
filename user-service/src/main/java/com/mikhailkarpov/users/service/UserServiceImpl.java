package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String USER_PROFILE_CACHE = "userProfile";

    private final UserProfileRepository userProfileRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public AccessTokenResponse authenticate(UserAuthenticationRequest request) {
        return keycloakAdminClient.obtainAccessToken(request.getUsername(), request.getPassword());
    }

    @Override
    @CachePut(value = USER_PROFILE_CACHE, key = "#result.id")
    @Transactional
    public UserProfileDto create(UserRegistrationRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        String email = request.getEmail();

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setCredentials(Collections.singletonList(credentials));
        user.setEmailVerified(true); // todo verify email
        user.setEnabled(true);

        String userId = this.keycloakAdminClient.createUser(user);
        UserProfile createdProfile = this.userProfileRepository.save(new UserProfile(userId, username, email));
        return new UserProfileDto(createdProfile);
    }

    @Override
    @Cacheable(value = USER_PROFILE_CACHE, key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> findById(String userId) {

        return this.userProfileRepository.findUserProfileById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findByUsernameLike(String username, Pageable pageable) {
        return this.userProfileRepository.findAllByUsernameContainingIgnoreCase(username, pageable);
    }
}
