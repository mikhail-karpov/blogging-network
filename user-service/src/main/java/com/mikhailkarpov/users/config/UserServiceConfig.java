package com.mikhailkarpov.users.config;

import com.mikhailkarpov.users.repository.UserProfileRepository;
import com.mikhailkarpov.users.service.KeycloakAdminClient;
import com.mikhailkarpov.users.service.UserService;
import com.mikhailkarpov.users.service.UserServiceCacheableImpl;
import com.mikhailkarpov.users.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceConfig {

    @Bean
    public UserService userService(UserProfileRepository repository, KeycloakAdminClient keycloakAdminClient) {
        UserService delegate = new UserServiceImpl(repository, keycloakAdminClient);
        return new UserServiceCacheableImpl(delegate);
    }
}
