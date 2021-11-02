package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FollowingEvent {

    public enum Status {
        FOLLOWED, UNFOLLOWED
    }

    @JsonProperty(value = "followerId")
    private String followerUserId;

    @JsonProperty(value = "followingId")
    private String followingUserId;

    @JsonProperty(value = "status")
    private Status status;
}
