package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import com.mikhailkarpov.bloggingnetwork.feed.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserFeedServiceTest extends AbstractIT {

    @MockBean
    private PostServiceClient postServiceClient;

    @Autowired
    private UserFeedService userFeedService;

    private final Post post1 = Post.builder()
            .id("post-1")
            .content("Post-1 content")
            .user(new UserProfile("user-1", "username-1"))
            .createdDate(LocalDateTime.now())
            .build();

    private final Post post2 = Post.builder()
            .id("post-2")
            .content("Post-2 content")
            .user(new UserProfile("user-2", "username-2"))
            .createdDate(LocalDateTime.now())
            .build();

    @Test
    void contextLoads() {
        //given
        Mockito.when(postServiceClient.getPostById("post-1")).thenReturn(Optional.of(post1));
        Mockito.when(postServiceClient.getPostById("post-2")).thenReturn(Optional.of(post2));

        //when
        userFeedService.startFollowing("follower", "user-1");
        userFeedService.startFollowing("follower", "user-2");
        userFeedService.addPost("user-1", "post-1");
        userFeedService.addPost("user-2", "post-2");

        //then
        assertIterableEquals(Collections.singletonList(post1), userFeedService.getUserFeed("follower", 1, 1));
        assertIterableEquals(Collections.singletonList(post2), userFeedService.getUserFeed("follower", 0, 1));

        //and when
        userFeedService.stopFollowing("follower", "user-2");
        userFeedService.addPost("user-2", "post-3");
        userFeedService.removePost("user-1", "post-1");

        //then
        assertIterableEquals(Arrays.asList(post2), userFeedService.getUserFeed("follower", 0, 10));
    }
}