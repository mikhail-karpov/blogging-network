package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class AbstractIT {

    static final KeycloakContainer KEYCLOAK;

    static {
        KEYCLOAK = new KeycloakContainer("jboss/keycloak:15.0.2")
                .withReuse(true);

        KEYCLOAK.start();
    }

    @DynamicPropertySource
    static void configDatasource(DynamicPropertyRegistry registry) {
        registry.add("app.keycloak.serverUrl", KEYCLOAK::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "master");
        registry.add("app.keycloak.user", KEYCLOAK::getAdminUsername);
        registry.add("app.keycloak.password", KEYCLOAK::getAdminPassword);
    }

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void cleanRepository() {
        postRepository.deleteAll();
    }
}
