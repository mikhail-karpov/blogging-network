package com.mikhailkarpov.bloggingnetwork.posts.domain;

import java.time.Instant;
import java.util.UUID;

public interface PostProjection {

    UUID getId();

    String getUserId();

    String getContent();

    Instant getCreatedDate();
}
