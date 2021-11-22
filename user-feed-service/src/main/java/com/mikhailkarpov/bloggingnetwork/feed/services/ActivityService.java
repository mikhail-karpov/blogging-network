package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.Activity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ActivityService {

    void save(Activity activity);

    void deleteById(ActivityId id);

    Optional<Activity> findById(ActivityId id);

    List<PostActivity> getFeed(String userId, int page);
}
