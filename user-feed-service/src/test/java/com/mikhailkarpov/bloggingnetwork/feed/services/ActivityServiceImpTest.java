package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class ActivityServiceImpTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EntityManager entityManager;

    private ActivityServiceImpl activityService;

    @BeforeEach
    void setUp() {
        this.activityService = new ActivityServiceImpl(this.activityRepository, this.entityManager);
    }

    @Test
    void givenActivities_whenGetUserFeed_thenPresent() {
        //given
        FollowingActivity followingActivity = new FollowingActivity("follower", "user");
        PostActivity postActivity1 = new PostActivity("post1", "user");
        PostActivity postActivity2 = new PostActivity("post2", "user");

        //when
        this.activityService.save(followingActivity);
        this.activityService.save(postActivity1);
        this.activityService.save(postActivity2);
        List<PostActivity> feed = this.activityService.getFeed("follower", 0);

        //then
        assertThat(feed.size()).isEqualTo(2);

        for (PostActivity activity : feed) {
            assertThat(activity.getAuthorId()).isEqualTo("user");
            assertThat(activity.getPostId()).contains("post");
        }
    }
}