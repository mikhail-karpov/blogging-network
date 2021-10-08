package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.util.OAuth2Client;
import com.mikhailkarpov.bloggingnetwork.posts.util.OAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class IntegrationTestSecurityConfig {

    @Autowired
    private OAuth2ProviderProperties oAuth2ProviderProperties;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Bean
    public OAuth2User oAuth2User(@Value("${app.keycloak.user}") String username,
                                 @Value("${app.keycloak.password}") String password) {
        return new OAuth2User(username, password);
    }

    @Bean
    public OAuth2Client oAuth2Client() {
        String serverUrl = oAuth2ProviderProperties.getServerUrl();
        String realm = oAuth2ProviderProperties.getRealm();
        return new OAuth2Client(serverUrl, realm, restTemplateBuilder.build());
    }
}
