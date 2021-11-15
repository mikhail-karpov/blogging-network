package com.mikhailkarpov.bloggingnetwork.feed.client;

import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PostServiceClientFallback implements PostServiceClient {

    public static final String DEFAULT = "Default";

    @Override
    public Optional<Post> getPostById(String postId) {

        UserProfile profile = new UserProfile();
        profile.setUserId(DEFAULT);
        profile.setUsername(DEFAULT);

        final Post post = Post.builder()
                .id(postId)
                .content(DEFAULT)
                .user(profile)
                .createdDate(LocalDateTime.now())
                .build();

        return Optional.of(post);
    }
}
