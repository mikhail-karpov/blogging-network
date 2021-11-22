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

    @JsonProperty("followerId")
    private String followerUserId;

    @JsonProperty("followingId")
    private String followingUserId;

    @JsonProperty("status")
    private Status status;
}
