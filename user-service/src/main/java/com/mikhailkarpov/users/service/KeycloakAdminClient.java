package com.mikhailkarpov.users.service;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface KeycloakAdminClient {

    String createUser(UserRepresentation user);

    UserRepresentation findUserById(String userId);

    List<UserRepresentation> findByUsernameLike(String username, int firstResult, int maxResults);

    AccessTokenResponse obtainAccessToken(String username, String password);
}
