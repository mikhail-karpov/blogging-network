package com.mikhailkarpov.bloggingnetwork.feed.repository;

import com.mikhailkarpov.bloggingnetwork.feed.domain.Activity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import org.springframework.data.repository.CrudRepository;

public interface ActivityRepository extends CrudRepository<Activity, ActivityId> {

}
