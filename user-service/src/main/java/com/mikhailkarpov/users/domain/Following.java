package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "following")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Following extends BaseEntity {

    @EmbeddedId
    private FollowingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile following;

    public Following(UserProfile follower, UserProfile following) {
        this.follower = follower;
        this.following = following;
        this.id = new FollowingId(follower.getId(), following.getId());
    }

    public String getFollowerUserId() {
        return this.id.getFollowerUserId();
    }

    public String getFollowingUserId() {
        return this.id.getFollowingUserId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Following following = (Following) o;

        return id.equals(following.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
