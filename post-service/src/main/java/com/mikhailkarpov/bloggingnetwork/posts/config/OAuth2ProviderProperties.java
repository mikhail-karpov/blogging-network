package com.mikhailkarpov.bloggingnetwork.posts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "app.keycloak")
@Getter
@Setter
public class OAuth2ProviderProperties {

    @NotBlank
    private String serverUrl;

    @NotBlank
    private String realm;
}
