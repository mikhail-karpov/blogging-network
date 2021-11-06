package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

public class AbstractControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
}
