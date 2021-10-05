package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class KeycloakAdminClientImplIT extends AbstractIT {

    @Autowired
    private KeycloakAdminClientImpl keycloakAdminClient;

    @Test
    void givenUserRepresentation_whenCreateUserAndFindById_thenCreatedAndFound() {
        //given
        String username = UUID.randomUUID().toString();
        String email = username + "@example.com";
        String password = UUID.randomUUID().toString();
        UserRepresentation user = createUser(username, email, password);

        //when
        String userId = keycloakAdminClient.createUser(user);

        //then
        assertThat(userId).isNotNull();

        //and when
        UserRepresentation foundUser = keycloakAdminClient.findUserById(userId);

        //then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    void givenNoUser_whenFindById_thenThrows() {
        //given
        String userId = UUID.randomUUID().toString();

        //then
        assertThrows(WebApplicationException.class, () -> keycloakAdminClient.findUserById(userId));
    }

    @Test
    void givenUser_whenObtainAccessToken_thenOk() {
        //given
        String username = UUID.randomUUID().toString();
        String email = username + "@example.com";
        String password = UUID.randomUUID().toString();
        UserRepresentation user = createUser(username, email, password);

        //when
        keycloakAdminClient.createUser(user);
        AccessTokenResponse accessTokenResponse = keycloakAdminClient.obtainAccessToken(username, password);

        //then
        assertThat(accessTokenResponse).isNotNull();
    }

    @Test
    void givenNoUser_whenObtainAccessToken_thenThrows() {
        //given
        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        //when
        assertThrows(WebApplicationException.class, () -> keycloakAdminClient.obtainAccessToken(username, password));
    }

    private UserRepresentation createUser(String username, String email, String password) {

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setCredentials(Collections.singletonList(credentials));
        user.setEmailVerified(true);
        user.setEnabled(true);

        return user;
    }
}