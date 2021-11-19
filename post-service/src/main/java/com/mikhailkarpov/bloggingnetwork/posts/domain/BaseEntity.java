package com.mikhailkarpov.bloggingnetwork.posts.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @CreatedDate
    private Instant createdDate;

    protected BaseEntity(String userId) {
        setId(UUID.randomUUID());
        setUserId(userId);
    }

    protected void setId(UUID id) {
        this.id = id;
    }

    protected void setUserId(String userId) {
        this.userId = Objects.requireNonNull(userId);
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public boolean isOwnedBy(String userId) {
        return this.userId.equals(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BaseEntity that = (BaseEntity) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
