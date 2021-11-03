package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;

public interface FollowingActivityService {

    void save(FollowingActivity activity);

    void delete(FollowingActivity activity);
}
