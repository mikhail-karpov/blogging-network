package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.Data;

@Data
public class PostActivity {

    private final String postId;
    private final String authorId;
}
