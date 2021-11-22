package com.mikhailkarpov.bloggingnetwork.feed.domain;

import com.google.common.base.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@NoArgsConstructor
@Getter
public class ActivityId implements Serializable {

    private static final long serialVersionUID = 5923132376167657703L;

    private String userId;

    private String sourceId;

    private ActivityType activityType;

    public ActivityId(String userId, String sourceId, ActivityType type) {
        this.userId = userId;
        this.sourceId = sourceId;
        this.activityType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityId that = (ActivityId) o;

        if (!userId.equals(that.userId)) return false;
        if (!sourceId.equals(that.sourceId)) return false;
        return activityType == that.activityType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, sourceId, activityType);
    }

    @Override
    public String toString() {
        return "ActivityId{" +
                "userId='" + userId + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", type=" + activityType +
                '}';
    }
}
