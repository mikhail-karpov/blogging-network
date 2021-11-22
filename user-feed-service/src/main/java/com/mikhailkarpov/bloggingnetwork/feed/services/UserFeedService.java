package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.dto.UserFeed;

public interface UserFeedService {

    UserFeed getUserFeed(String userId, int page);
}
