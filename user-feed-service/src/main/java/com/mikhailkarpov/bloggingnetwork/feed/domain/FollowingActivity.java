package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("FOLLOWING_ACTIVITY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowingActivity extends Activity {

    public FollowingActivity(String followerUserId, String followingUserId) {
        super(followerUserId, followingUserId, ActivityType.FOLLOWING_ACTIVITY);
    }

    public String getFollowerUserId() {
        return getUserId();
    }

    public String getFollowingUserId() {
        return getSourceId();
    }
}
