package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "following")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Following extends BaseEntity {

    @EmbeddedId
    private FollowingId followingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfile user;

    public Following(UserProfile follower, UserProfile user) {
        this.follower = follower;
        this.user = user;
        this.followingId = new FollowingId(follower.getId(), user.getId());
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
