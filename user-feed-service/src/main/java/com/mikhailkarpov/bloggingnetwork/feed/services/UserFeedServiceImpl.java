package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.client.PostServiceClient;
import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFeedServiceImpl implements UserFeedService {

    private final PostActivityService postActivityService;

    private final PostServiceClient postServiceClient;

    @Override
    public List<Post> getUserFeed(String userId, Pageable pageable) {

        List<Post> entries = new ArrayList<>(pageable.getPageSize());

        this.postActivityService.getFeed(userId, pageable).forEach(postActivity -> {
            Optional<Post> post = this.postServiceClient.getPostById(postActivity.getPostId());
            if (post.isPresent()) {
                entries.add(post.get());
            }
        });

        return entries;
    }
}
