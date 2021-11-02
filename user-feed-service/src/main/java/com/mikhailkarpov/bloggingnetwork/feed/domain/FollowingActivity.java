package com.mikhailkarpov.bloggingnetwork.feed.domain;

public class FollowingActivity extends AbstractActivity {

    public FollowingActivity(String followerUserId, String followingUserId) {
        super(followerUserId, followingUserId, ActivityType.FOLLOWING_ACTIVITY);
    }

    public String getFollowerUserId() {
        return this.userId;
    }

    public String getFollowingUserId() {
        return this.sourceId;
    }
}
