package com.mikhailkarpov.users.config;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
@Getter
@Setter
public class KeycloakAdminConfig {

    @NotBlank
    private String serverUrl;

    @NotBlank
    private String realm;

    @NotBlank
    private String adminUsername;

    @NotBlank
    private String adminPassword;

    @Bean(destroyMethod = "close")
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    @Bean
    public UsersResource usersResource(Keycloak keycloak) {
        return keycloak.realm(realm).users();
    }
}
