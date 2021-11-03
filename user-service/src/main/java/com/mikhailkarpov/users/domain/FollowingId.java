package com.mikhailkarpov.users.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
@AllArgsConstructor
public class FollowingId implements Serializable {

    private String followerId;

    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FollowingId that = (FollowingId) o;

        if (!followerId.equals(that.followerId)) return false;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        int result = followerId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}
