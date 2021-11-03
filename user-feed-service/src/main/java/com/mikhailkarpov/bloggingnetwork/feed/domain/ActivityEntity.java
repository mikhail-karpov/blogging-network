package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.time.Instant;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityEntity {

    @EmbeddedId
    private ActivityId id;

    @Column(name = "created_date")
    private Instant createdDate;

    public ActivityEntity(String userId, String sourceId, ActivityType type) {
        this.id = new ActivityId(userId, sourceId, type);
        this.createdDate = Instant.now();
    }

    public String getUserId() {
        return this.id.getUserId();
    }

    public String getSourceId() {
        return this.id.getSourceId();
    }

    public ActivityType getActivityType() {
        return this.id.getType();
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityEntity entity = (ActivityEntity) o;

        return id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ActivityEntity{" +
                "userId=" + getUserId() +
                ", sourceId=" + getSourceId() +
                ", type=" + getActivityType() +
                '}';
    }
}
