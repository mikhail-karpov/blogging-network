package com.mikhailkarpov.bloggingnetwork.feed.client;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PostServiceClientFallbackTest {

    @Test
    void shouldReturnEmpty() {
        PostServiceClientFallback fallback = new PostServiceClientFallback();

        assertFalse(fallback.getPostById(UUID.randomUUID().toString()).isPresent());
    }
}