package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.KeycloakAdminConfig;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties
@ContextConfiguration(classes = {KeycloakAdminConfig.class, KeycloakAdminClientImpl.class})
class KeycloakAdminClientIT {

    static final KeycloakContainer KEYCLOAK;

    static {
        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withRealmImportFile("./bloggingnetwork-realm.json");

        KEYCLOAK.start();
    }

    @DynamicPropertySource
    static void configKeycloak(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "bloggingnetwork");
        registry.add("app.keycloak.adminUsername", () -> "admin");
        registry.add("app.keycloak.adminPassword", () -> "admin");
    }

    @Autowired
    private KeycloakAdminClientImpl keycloakAdminClient;

    @Test
    void givenUserRepresentation_whenCreateUserAndFindById_thenCreatedAndFound() {
        //given
        String username = UUID.randomUUID().toString();
        String email = username + "@example.com";
        String password = UUID.randomUUID().toString();
        UserRepresentation user = buildUserRepresentation(username, email, password);

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
    void givenUserRepresentation_whenFindByUsernameLike_thenFound() {
        //given
        UserRepresentation user1 = buildUserRepresentation("hugoboss", "hugoboss@example.com", "password");
        UserRepresentation user2 = buildUserRepresentation("boss", "boss@example.com", "password");

        //when
        this.keycloakAdminClient.createUser(user1);
        this.keycloakAdminClient.createUser(user2);
        List<UserRepresentation> users =
                this.keycloakAdminClient.findByUsernameLike("Boss", 0, 10);

        //then
        assertThat(users.size()).isEqualTo(2);
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
        UserRepresentation user = buildUserRepresentation(username, email, password);

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

    private UserRepresentation buildUserRepresentation(String username, String email, String password) {

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