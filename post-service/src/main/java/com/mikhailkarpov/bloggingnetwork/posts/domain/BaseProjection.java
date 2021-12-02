package com.mikhailkarpov.bloggingnetwork.posts.domain;

import java.time.Instant;
import java.util.UUID;

public interface BaseProjection {

    UUID getId();

    String getUserId();

    Instant getCreatedDate();
}
