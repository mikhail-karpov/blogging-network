package com.mikhailkarpov.users.service;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakAdminClient {
    String createUser(UserRepresentation user);

    UserRepresentation findUserById(String userId);

    AccessTokenResponse obtainAccessToken(String username, String password);
}
