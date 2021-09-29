package com.mikhailkarpov.users.service;

import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakAdminClient {
    String createUser(UserRepresentation user);

    UserRepresentation findUserById(String userId);
}
