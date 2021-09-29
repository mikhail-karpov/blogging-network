package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    public static final String USER_PROFILE_CACHE = "userProfile";

    @Override
    @CachePut(value = USER_PROFILE_CACHE, key = "#result.id")
    public UserProfile create(UserRegistrationRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();

        if (userProfileRepository.existsByUsernameOrEmail(username, email)) {
            throw new ResourceAlreadyExistsException("User already exists");
        }

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

        String userId = keycloakAdminClient.createUser(user);
        return userProfileRepository.save(new UserProfile(userId, username, email));
    }

    @Override
    public Iterable<UserProfile> findByIdIn(List<String> idList) {

        return userProfileRepository.findAllById(idList);
    }

    @Override
    @Cacheable(value = USER_PROFILE_CACHE, key = "#userId", unless = "#result == null")
    public Optional<UserProfile> findById(String userId) {

        return userProfileRepository.findById(userId);
    }
}
