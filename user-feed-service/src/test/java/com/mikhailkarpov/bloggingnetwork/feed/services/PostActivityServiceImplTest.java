package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class PostActivityServiceImplTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EntityManager entityManager;

    private PostActivityServiceImpl postActivityService;

    @BeforeEach
    void setUp() {
        this.postActivityService = new PostActivityServiceImpl(this.activityRepository, this.entityManager);
    }

    @Test
    void givenActivities_whenGetFollowersPosts_thenFound() {
        //given
        this.activityRepository.save(new ActivityEntity("follower-id", "user-id", FOLLOWING_ACTIVITY));

        for (int i = 1; i <= 10; i++) {
            PostActivity postActivity = new PostActivity("post " + i, "user-id");
            postActivityService.save(postActivity);
        }

        //when
        List<PostActivity> postActivities = postActivityService.getFeed("follower-id", PageRequest.of(0, 10));

        //then
        assertThat(postActivities.size()).isEqualTo(10);
    }
}