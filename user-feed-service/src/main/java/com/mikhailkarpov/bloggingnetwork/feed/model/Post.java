package com.mikhailkarpov.bloggingnetwork.feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash(value = "post", timeToLive = 60L * 60 * 24 * 7)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    private String id;

    private String content;

    private UserProfile user;

    private LocalDateTime createdDate;

    @Builder
    private Post(String id, String content, UserProfile user, LocalDateTime createdDate) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.createdDate = createdDate;
    }

}
