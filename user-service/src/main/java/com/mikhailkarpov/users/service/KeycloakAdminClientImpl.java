package com.mikhailkarpov.users.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service
@RequiredArgsConstructor
public class KeycloakAdminClientImpl implements KeycloakAdminClient {

    private final UsersResource usersResource;

    @Override
    public String createUser(UserRepresentation user) {

        try (Response response = usersResource.create(user)) {
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    @Override
    @Cacheable(value = "userProfile", key = "#userId")
    public UserRepresentation findUserById(String userId) {

        return usersResource.get(userId).toRepresentation();
    }
}
