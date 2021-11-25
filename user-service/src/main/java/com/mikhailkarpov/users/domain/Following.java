package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "following")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Following extends BaseEntity {

    @EmbeddedId
    private FollowingId followingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile followerUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile followingUser;

    public Following(UserProfile followerUser, UserProfile followingUser) {
        this.followerUser = followerUser;
        this.followingUser = followingUser;
        this.followingId = new FollowingId(followerUser.getId(), followingUser.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Following following = (Following) o;

        return followingId.equals(following.followingId);
    }

    @Override
    public int hashCode() {
        return followingId.hashCode();
    }

    @Override
    public String toString() {
        return "Following{" +
                "followingId=" + followingId +
                ", createdDate=" + getCreatedDate() +
                '}';
    }
}
