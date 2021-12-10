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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserFeedServiceIT extends AbstractIT {

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
        Mockito.when(this.postServiceClient.getPostById("post-1")).thenReturn(Optional.of(post1));
        Mockito.when(this.postServiceClient.getPostById("post-2")).thenReturn(Optional.of(post2));

        //when
        this.userFeedService.startFollowing("follower", "user-1");
        this.userFeedService.startFollowing("follower", "user-2");
        this.userFeedService.addPost("user-1", "post-1");
        this.userFeedService.addPost("user-2", "post-2");

        assertTrue(this.userFeedService.getUserFeed("follower", 0, 2).isEmpty());

        this.userFeedService.generateUserFeed("follower");
        List<Post> feed = this.userFeedService.getUserFeed("follower", 1, 1);

        //then
        assertIterableEquals(Collections.singletonList(post1), feed);

        //and when
        this.userFeedService.stopFollowing("follower", "user-2");
        this.userFeedService.addPost("user-2", "post-3");
        this.userFeedService.removePost("user-1", "post-1");
        this.userFeedService.generateUserFeed("follower");
        feed = this.userFeedService.getUserFeed("follower", 0, 10);

        //then
        assertIterableEquals(Arrays.asList(post2, post1), feed);
    }
}