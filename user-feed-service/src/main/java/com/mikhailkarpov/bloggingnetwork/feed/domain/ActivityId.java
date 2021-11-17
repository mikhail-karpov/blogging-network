package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ActivityId implements Serializable {

    private static final long serialVersionUID = 5923132376167657703L;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Column(name = "activity_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType type;

    public ActivityId(String userId, String sourceId, ActivityType type) {
        this.userId = userId;
        this.sourceId = sourceId;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityId that = (ActivityId) o;

        if (!userId.equals(that.userId)) return false;
        if (!sourceId.equals(that.sourceId)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + sourceId.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ActivityId{" +
                "userId='" + userId + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", type=" + type +
                '}';
    }
}
