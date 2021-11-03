package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostActivityServiceImplTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void givenActivities_whenGetFollowersPosts_thenFound() {
        //given
        PostActivityService postActivityService = new PostActivityServiceImpl(
                this.activityRepository, this.entityManager);

        this.activityRepository.save(new ActivityEntity("follower-id", "user-id", FOLLOWING_ACTIVITY));

        for (int i = 1; i <= 10; i++) {
            PostActivity postActivity = new PostActivity("post " + i, "user-id");
            postActivityService.save(postActivity);
        }

        //when
        List<PostActivity> posts = new ArrayList<>();
        postActivityService.getFeed("follower-id", PageRequest.of(3, 3)).forEach(posts::add);

        //then
        assertThat(posts.size()).isEqualTo(1);
    }
}