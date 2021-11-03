package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.Data;

@Data
public class FollowingActivity {

    private final String followerUserId;
    private final String followingUserId;
}
