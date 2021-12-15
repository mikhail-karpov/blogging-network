package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.KeycloakAdminConfig;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

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
    private KeycloakAdminClient keycloakAdminClient;

    @Test
    void givenUserRepresentation_whenCreateUserAndFindById_thenCreatedAndFound() {
        //given
        String username = UUID.randomUUID().toString();
        String email = username + "@example.com";
        String password = UUID.randomUUID().toString();
        UserRepresentation user = buildUserRepresentation(username, email, password);

        //when
        String userId = keycloakAdminClient.createUser(user);
        UserRepresentation foundUser = keycloakAdminClient.findUserById(userId);

        //then
        assertNotNull(foundUser.getId());
        assertEquals(username, foundUser.getUsername());
        assertEquals(email, foundUser.getEmail());
    }

    @Test
    void givenDuplicateUsername_whenCreateUser_thenResourceAlreadyExistsException() {
        //given
        String username = UUID.randomUUID().toString();

        UserRepresentation user =
                buildUserRepresentation(username, String.format("%s@example.com", username), randomAlphabetic(10));

        UserRepresentation duplicateUsernameUser =
                buildUserRepresentation(username, String.format("%s@fake.com", username), randomAlphabetic(10));

        //when
        Executable createDuplicateUsernameUser = () -> keycloakAdminClient.createUser(duplicateUsernameUser);

        //then
        assertNotNull(keycloakAdminClient.createUser(user));
        assertThrows(ResourceAlreadyExistsException.class, createDuplicateUsernameUser);
    }

    @Test
    void givenDuplicateEmail_whenCreateUser_thenResourceAlreadyExistsException() {
        //given
        String email = String.format("%s@example.com", randomAlphabetic(10));

        UserRepresentation user =
                buildUserRepresentation(randomAlphabetic(10), email, randomAlphabetic(10));

        UserRepresentation duplicateEmailUser =
                buildUserRepresentation(randomAlphabetic(10), email, randomAlphabetic(10));

        //when
        Executable createDuplicateUsernameUser = () -> keycloakAdminClient.createUser(duplicateEmailUser);

        //then
        assertNotNull(keycloakAdminClient.createUser(user));
        assertThrows(ResourceAlreadyExistsException.class, createDuplicateUsernameUser);
    }

    @Test
    void givenUserRepresentation_whenFindByUsernameLike_thenFound() {
        //given
        UserRepresentation user1 =
                buildUserRepresentation("hugoboss", "hugoboss@example.com", "password");

        UserRepresentation user2 =
                buildUserRepresentation("boss", "boss@example.com", "password");

        //when
        this.keycloakAdminClient.createUser(user1);
        this.keycloakAdminClient.createUser(user2);
        List<UserRepresentation> foundUsers =
                this.keycloakAdminClient.findByUsernameLike("Boss", 0, 10);

        //then
        assertEquals(2, foundUsers.size());
    }

    @Test
    void givenNoUser_whenFindById_thenResourceNotFoundException() {
        //given
        String userId = UUID.randomUUID().toString();

        //then
        assertThrows(ResourceNotFoundException.class, () -> keycloakAdminClient.findUserById(userId));
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
        assertNotNull(accessTokenResponse);
    }

    @Test
    void givenNoUser_whenObtainAccessToken_thenBadCredentialsException() {
        //given
        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        //when
        assertThrows(BadCredentialsException.class, () -> keycloakAdminClient.obtainAccessToken(username, password));
    }

    protected final UserRepresentation buildUserRepresentation(String username, String email, String password) {

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