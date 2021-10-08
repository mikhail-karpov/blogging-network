package com.mikhailkarpov.bloggingnetwork.posts.util;

import lombok.Value;

@Value
public class OAuth2User {

    private final String username;
    private final String password;
}
