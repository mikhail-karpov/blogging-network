package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FollowingActivityServiceImplTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TestEntityManager entityManager;

    private FollowingActivityServiceImpl followingActivityService;

    @BeforeEach
    void setUp() {
        this.followingActivityService = new FollowingActivityServiceImpl(this.activityRepository);
    }

    @Test
    void givenActivity_whenSaved_thenFound() {
        //given
        FollowingActivity activity = new FollowingActivity("follower", "user");
        ActivityId activityId = new ActivityId("follower", "user", ActivityType.FOLLOWING_ACTIVITY);

        //when
        this.followingActivityService.save(activity);

        //then
        assertThat(this.entityManager.find(ActivityEntity.class, activityId)).isNotNull();
    }

    @Test
    void givenActivity_whenDeleted_thenNotFound() {
        //given
        ActivityEntity activityEntity = new ActivityEntity("follower", "user", ActivityType.FOLLOWING_ACTIVITY);
        ActivityId activityId = new ActivityId("follower", "user", ActivityType.FOLLOWING_ACTIVITY);
        this.entityManager.persistAndFlush(activityEntity);

        //when
        this.followingActivityService.delete(new FollowingActivity("follower", "user"));
        ActivityEntity found = this.entityManager.find(ActivityEntity.class, activityId);

        //then
        assertThat(this.entityManager.find(ActivityEntity.class, activityId)).isNull();
    }
}