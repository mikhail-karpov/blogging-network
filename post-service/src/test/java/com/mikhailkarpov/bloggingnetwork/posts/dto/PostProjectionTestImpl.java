package com.mikhailkarpov.bloggingnetwork.posts.dto;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class PostProjectionTestImpl implements PostProjection {

    private UUID id;

    private String userId;

    private Instant createdDate;

    private String content;
}
