package com.mikhailkarpov.users.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FollowingNotification implements Notification {

    public enum Status {
        FOLLOWED, UNFOLLOWED
    }

    private String followerId;
    private String followingId;
    private Status status;

    public FollowingNotification(String followerId, String followingId, Status status) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.status = status;
    }

    @Override
    public String getType() {
        return status.name();
    }
}
