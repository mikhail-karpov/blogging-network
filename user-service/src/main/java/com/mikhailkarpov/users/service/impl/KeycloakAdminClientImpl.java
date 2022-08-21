package com.mikhailkarpov.users.service.impl;

import com.mikhailkarpov.users.config.KeycloakAdminConfig;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.service.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakAdminClientImpl implements KeycloakAdminClient {

    private final UsersResource usersResource;
    private final KeycloakAdminConfig keycloakAdminConfig;

    @Override
    public String createUser(UserRepresentation user) {

        Response response = usersResource.create(user);

        if (response.getStatus() == 409) {
            throw new ResourceAlreadyExistsException("User already exists");
        }

        return CreatedResponseUtil.getCreatedId(response);
    }

    @Override
    public UserRepresentation findUserById(String userId) {

        try {
            return usersResource.get(userId).toRepresentation();

        } catch (NotFoundException e) {
            String message = String.format("User with id='%s' not found", userId);
            throw new ResourceNotFoundException(message);
        }
    }

    @Override
    public List<UserRepresentation> findByUsernameLike(String username, int firstResult, int maxResults) {

        return usersResource.search(username, firstResult, maxResults, true);
    }

    @Override
    public AccessTokenResponse obtainAccessToken(String username, String password) {

        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakAdminConfig.getServerUrl())
                .realm(keycloakAdminConfig.getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .username(username)
                .password(password)
                .build()) {

            return keycloak.tokenManager().getAccessToken();

        } catch (NotAuthorizedException e) {
            throw new BadCredentialsException("Bad credentials", e);
        }
    }
}
