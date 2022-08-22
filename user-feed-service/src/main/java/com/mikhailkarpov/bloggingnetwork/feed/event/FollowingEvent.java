package com.mikhailkarpov.bloggingnetwork.feed.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
