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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public AccessTokenResponse authenticateUser(UserAuthenticationRequest request) {
        return keycloakAdminClient.obtainAccessToken(request.getUsername(), request.getPassword());
    }

    @Override
    @Transactional
    public UserProfileDto registerUser(UserRegistrationRequest request) {

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
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> findUserById(String userId) {

        return this.userProfileRepository.findUserProfileById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findUsersByUsernameLike(String username, Pageable pageable) {
        return this.userProfileRepository.findAllByUsernameContainingIgnoreCase(username, pageable);
    }
}
