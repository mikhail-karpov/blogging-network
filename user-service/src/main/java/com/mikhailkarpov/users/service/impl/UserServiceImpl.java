package com.mikhailkarpov.users.service.impl;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.AccessTokenDto;
import com.mikhailkarpov.users.dto.SignInRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.SignUpRequest;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import com.mikhailkarpov.users.service.KeycloakAdminClient;
import com.mikhailkarpov.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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

    private final UserProfileRepository userProfileRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> findUserById(String userId) {

        return this.userProfileRepository.findUserProfileById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findUsersByUsernameLike(String username, Pageable pageable) {
        return this.userProfileRepository.findAllByUsernameContainingIgnoreCase(username, pageable);
    }

    @Override
    public AccessTokenDto signIn(SignInRequest request) {
        String username = request.getUsername();
        String password = new String(request.getPassword());

        String accessToken = keycloakAdminClient.obtainAccessToken(username, password).getToken();
        return new AccessTokenDto(accessToken);
    }

    @Override
    @Transactional
    public UserProfileDto signUp(SignUpRequest request) {

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(new String(request.getPassword()));

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(Collections.singletonList(credentials));
        user.setEmailVerified(true); // todo verify email
        user.setEnabled(true);

        String userId = this.keycloakAdminClient.createUser(user);
        UserProfile createdProfile = this.userProfileRepository.save(new UserProfile(userId, request.getUsername(), request.getEmail()));
        return new UserProfileDto(createdProfile);
    }
}
