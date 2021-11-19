package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "following")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Following {

    @EmbeddedId
    private FollowingId followingId;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
                ", createdDate=" + createdDate +
                '}';
    }
}
