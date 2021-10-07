package com.mikhailkarpov.bloggingnetwork.posts.util;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

public class EntityUtils {

    public static Post createRandomPost(int charactersLong) {
        String userId = UUID.randomUUID().toString();
        String content = RandomStringUtils.randomAlphabetic(charactersLong);
        return new Post(userId, content);
    }

    public static PostComment createRandomPostComment(int charactersLong) {
        String userId = UUID.randomUUID().toString();
        String content = RandomStringUtils.randomAlphabetic(charactersLong);
        return new PostComment(userId, content);
    }
}
