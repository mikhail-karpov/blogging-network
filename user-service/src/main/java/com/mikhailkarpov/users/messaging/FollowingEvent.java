package com.mikhailkarpov.users.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FollowingEvent {

    public enum Status {
        FOLLOWED, UNFOLLOWED
    }

    private String followerId;
    private String followingId;
    private Status status;

    public FollowingEvent(String followerId, String followingId, Status status) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.status = status;
    }
}
