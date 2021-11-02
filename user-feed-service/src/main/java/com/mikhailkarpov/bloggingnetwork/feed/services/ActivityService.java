package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.AbstractActivity;

public interface ActivityService<A extends AbstractActivity> {

    void saveActivity(A activity);

    void deleteActivity(A activity);
}
