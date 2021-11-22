package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFeedServiceImpl implements UserFeedService {

    private final ActivityService activityService;

    private final PostServiceClient postServiceClient;

    @Override
    public UserFeed getUserFeed(String userId, int page) {

        List<Post> posts = this.activityService.getFeed(userId, page)
                .stream()
                .map(postActivity -> this.postServiceClient.getPostById(postActivity.getPostId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new UserFeed(posts);
    }
}
