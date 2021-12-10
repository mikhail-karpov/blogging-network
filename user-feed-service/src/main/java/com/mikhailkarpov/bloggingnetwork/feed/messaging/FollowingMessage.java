package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowingMessage {

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
