package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserProfile;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFeedServiceImplTest {

    @Mock
    private PostActivityService postActivityService;

    @Mock
    private PostServiceClient postServiceClient;

    @InjectMocks
    private UserFeedServiceImpl userFeedService;

    @Test
    void givenActivitiesAndPosts_whenGetUserFeed_thenFeedIsReturned() {
        //given
        String userId = "user-id";
        PageRequest pageRequest = PageRequest.of(1, 3);

        Post post1 = Post.builder()
                .id("post1")
                .content("Post 1 content")
                .user(new UserProfile("user1", "username1"))
                .createdDate(LocalDateTime.now().minus(2L, ChronoUnit.DAYS))
                .build();

        Post post2 = Post.builder()
                .id("post2")
                .content("Post 2 content")
                .user(new UserProfile("user2", "username1"))
                .createdDate(LocalDateTime.now().minus(1L, ChronoUnit.DAYS))
                .build();

        when(this.postActivityService.getFeed(userId, pageRequest)).thenReturn(Arrays.asList(
                new PostActivity("post1", "user1"),
                new PostActivity("post2", "user2")
        ));
        when(this.postServiceClient.getPostById("post1")).thenReturn(Optional.of(post1));
        when(this.postServiceClient.getPostById("post2")).thenReturn(Optional.of(post2));

        //when
        List<Post> posts = this.userFeedService.getUserFeed(userId, pageRequest);

        //then
        Assertions.assertThat(posts).containsExactly(post1, post2);
    }

    @Test
    void givenActivitiesAndNoPosts_whenGetUserFeed_thenFeedIsEmpty() {
        //given
        String userId = "user-id";
        PageRequest pageRequest = PageRequest.of(1, 3);

        when(this.postActivityService.getFeed(userId, pageRequest)).thenReturn(Arrays.asList(
                new PostActivity("post1", "user1"),
                new PostActivity("post2", "user2")
        ));
        when(this.postServiceClient.getPostById(anyString())).thenReturn(Optional.empty());

        //when
        List<Post> posts = this.userFeedService.getUserFeed(userId, pageRequest);

        //then
        Assertions.assertThat(posts).isEmpty();
    }
}