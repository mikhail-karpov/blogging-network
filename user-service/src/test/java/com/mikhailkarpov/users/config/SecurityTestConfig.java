package com.mikhailkarpov.users.config;

import com.mikhailkarpov.users.util.OAuth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityTestConfig {

    @Autowired
    private KeycloakConfig keycloakConfig;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Bean
    public OAuth2Utils oAuth2Utils() {
        return new OAuth2Utils(restTemplateBuilder.build(), keycloakConfig.getServerUrl(), keycloakConfig.getRealm());
    }
}
