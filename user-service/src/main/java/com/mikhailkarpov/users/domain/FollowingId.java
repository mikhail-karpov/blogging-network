package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
@AllArgsConstructor
public class FollowingId implements Serializable {

    @Column(name = "follower_user_id", nullable = false, updatable = false)
    private String followerUserId;

    @Column(name = "following_user_id", nullable = false, updatable = false)
    private String followingUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FollowingId that = (FollowingId) o;

        if (!followerUserId.equals(that.followerUserId)) return false;
        return followingUserId.equals(that.followingUserId);
    }

    @Override
    public int hashCode() {
        int result = followerUserId.hashCode();
        result = 31 * result + followingUserId.hashCode();
        return result;
    }
}
