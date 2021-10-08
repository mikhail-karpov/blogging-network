package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.config.DtoMapperConfig;
import com.mikhailkarpov.bloggingnetwork.posts.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = {DtoMapperConfig.class, TestSecurityConfig.class})
public abstract class AbstractControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
}
