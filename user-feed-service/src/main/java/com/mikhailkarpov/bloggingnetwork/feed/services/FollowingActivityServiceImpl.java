package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowingActivityServiceImpl implements FollowingActivityService {

    private final ActivityRepository activityRepository;

    @Override
    public void save(FollowingActivity activity) {
        String followerUserId = activity.getFollowerUserId();
        String followingUserId = activity.getFollowingUserId();

        ActivityEntity entity = new ActivityEntity(followerUserId, followingUserId, FOLLOWING_ACTIVITY);
        this.activityRepository.save(entity);
        log.info("Saving {}", entity);
    }

    @Override
    public void delete(FollowingActivity activity) {
        String followerUserId = activity.getFollowerUserId();
        String followingUserId = activity.getFollowingUserId();

        ActivityId id = new ActivityId(followerUserId, followingUserId, FOLLOWING_ACTIVITY);
        this.activityRepository.deleteById(id);
        log.info("Delete by id={}", id);
    }
}
