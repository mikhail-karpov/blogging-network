package com.mikhailkarpov.users.contract;

import com.mikhailkarpov.users.api.UserController;
import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.service.UserService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.AdditionalMatchers.not;

@WithMockUser
@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
public class UsersBase {

    @MockBean
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setContext() {
        RestAssuredMockMvc.webAppContextSetup(this.context);
    }

    @BeforeEach
    void setUpUserService() {
        String userId = UUID.randomUUID().toString();
        String username = RandomStringUtils.randomAlphabetic(10);
        String email = username + "@email.com";

        Mockito.when(this.userService.findById("0"))
                .thenReturn(Optional.empty());

        Mockito.when(this.userService.findById(not(ArgumentMatchers.eq("0"))))
                .thenReturn(Optional.of(new UserProfile(userId, username, email)));
    }
}
