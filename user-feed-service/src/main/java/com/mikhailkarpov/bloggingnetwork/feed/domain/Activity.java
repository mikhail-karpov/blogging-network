package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "activity_type")
@EntityListeners(AuditingEntityListener.class)
@Table(name = "activity")
@IdClass(ActivityId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Activity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Id
    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Id
    @Column(name = "activity_type", nullable = false, updatable = false, insertable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    public Activity(String userId, String sourceId, ActivityType activityType) {
        this.userId = userId;
        this.sourceId = sourceId;
        this.activityType = activityType;
    }

    String getUserId() {
        return userId;
    }

    String getSourceId() {
        return sourceId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Activity that = (Activity) o;

        if (!userId.equals(that.userId))
            return false;
        if (!sourceId.equals(that.sourceId))
            return false;
        if (activityType != that.activityType)
            return false;
        return Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + sourceId.hashCode();
        result = 31 * result + activityType.hashCode();
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        return result;
    }
}
