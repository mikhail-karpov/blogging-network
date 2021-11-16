package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserFeedService {

    List<Post> getUserFeed(String userId, Pageable pageable);
}
