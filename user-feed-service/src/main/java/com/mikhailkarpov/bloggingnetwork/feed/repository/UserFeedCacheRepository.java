package com.mikhailkarpov.bloggingnetwork.feed.repository;

import com.mikhailkarpov.bloggingnetwork.feed.model.UserFeed;
import org.springframework.data.repository.CrudRepository;

public interface UserFeedCacheRepository extends CrudRepository<UserFeed, String> {
}
