package com.mikhailkarpov.users.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowingId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "follower_user_id", nullable = false, updatable = false)
    private String followerUserId;

    @Column(name = "following_user_id", nullable = false, updatable = false)
    private String followingUserId;
}
