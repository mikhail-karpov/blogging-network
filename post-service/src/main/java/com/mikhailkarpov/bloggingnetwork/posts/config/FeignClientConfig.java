package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.client.OAuth2RequestInterceptor;
import com.mikhailkarpov.bloggingnetwork.posts.client.UserServiceClient;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableFeignClients(clients = UserServiceClient.class)
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor oauth2RequestInterceptor(ClientRegistrationRepository clientRegistrationRepository) {
        return new OAuth2RequestInterceptor(clientRegistrationRepository.findByRegistrationId("keycloak"));
    }
}
