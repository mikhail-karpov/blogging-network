package com.mikhailkarpov.bloggingnetwork.feed.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash("post")
@Getter
public class Post {

    @Id
    private final String id;

    private final String content;

    private final UserProfile user;

    private final LocalDateTime createdDate;

    @Builder
    public Post(String id, String content, UserProfile user, LocalDateTime createdDate) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return id.equals(post.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
