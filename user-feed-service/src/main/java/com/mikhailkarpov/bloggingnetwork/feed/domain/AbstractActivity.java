package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.Data;

@Data
public abstract class AbstractActivity {

    final String userId;
    final String sourceId;
    final ActivityType activityType;

}
