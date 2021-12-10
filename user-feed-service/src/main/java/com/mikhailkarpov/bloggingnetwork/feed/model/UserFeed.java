package com.mikhailkarpov.bloggingnetwork.feed.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash("userFeed")
public class UserFeed {

    @Id
    private final String userId;

    @Reference
    private final List<Post> posts;

    public UserFeed(String userId, List<Post> posts) {
        this.userId = userId;
        this.posts = posts;
    }

    public List<Post> getPosts(int page, int size) {

        int start = page * size;
        int end = start + size;
        if (start < 0)
            start = 0;
        if (end > posts.size())
            end = posts.size();
        if (start > end)
            start = end;

        return posts.subList(start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserFeed userFeed = (UserFeed) o;

        return userId.equals(userFeed.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}
