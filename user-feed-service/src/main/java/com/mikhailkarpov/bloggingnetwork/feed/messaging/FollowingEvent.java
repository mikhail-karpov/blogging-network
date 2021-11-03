package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;

public class FollowingEvent extends FollowingActivity {

    public FollowingEvent(@JsonProperty("followerId") String followerUserId,
                          @JsonProperty("followingId") String followingUserId,
                          @JsonProperty("status") Status status) {
        super(followerUserId, followingUserId);
        this.status = status;
    }

    private final Status status;

    public enum Status {
        FOLLOWED, UNFOLLOWED
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FollowingEvent event = (FollowingEvent) o;

        return super.equals(o) && status == event.status;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FollowingEvent{" +
                "followerUserId=" + getFollowerUserId() +
                ", followingUserId=" + getFollowingUserId() +
                ", status=" + status +
                '}';
    }
}
