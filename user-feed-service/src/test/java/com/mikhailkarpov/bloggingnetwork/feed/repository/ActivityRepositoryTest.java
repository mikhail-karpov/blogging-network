package com.mikhailkarpov.bloggingnetwork.feed.repository;

import com.mikhailkarpov.bloggingnetwork.feed.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.Activity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.POST_ACTIVITY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Test
    void givenActivity_whenSaved_thenFound() {
        //given
        Activity activity = new PostActivity("post-id", "author-id");
        this.activityRepository.save(activity);

        //when
        ActivityId id = new ActivityId("author-id", "post-id", POST_ACTIVITY);

        //then
        assertThat(this.activityRepository.findById(id)).isPresent();
    }

    @Test
    void givenSavedActivity_whenDeleted_thenNotFound() {
        //given
        Activity activity = new FollowingActivity("follower-id", "user-id");
        this.activityRepository.save(activity);

        //when
        ActivityId id = new ActivityId("follower-id", "user-id", FOLLOWING_ACTIVITY);
        this.activityRepository.deleteById(id);

        //then
        assertThat(this.activityRepository.findById(id)).isEmpty();
    }
}