package com.mikhailkarpov.bloggingnetwork.feed.client;

import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostServiceClientFallback implements PostServiceClient {

    @Override
    public Optional<Post> getPostById(String postId) {

        return Optional.empty();
    }
}
