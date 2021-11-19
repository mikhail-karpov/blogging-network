package com.mikhailkarpov.bloggingnetwork.feed.repository;

import com.mikhailkarpov.bloggingnetwork.feed.config.PersistenceTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

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
        ActivityEntity activity = new ActivityEntity("user-id", "source-id", POST_ACTIVITY);
        this.activityRepository.save(activity);

        //when
        ActivityId id = new ActivityId("user-id", "source-id", POST_ACTIVITY);
        Optional<ActivityEntity> foundActivity = this.activityRepository.findById(id);

        //then
        assertThat(foundActivity).isPresent();
        assertThat(foundActivity.get().getUserId()).isEqualTo("user-id");
        assertThat(foundActivity.get().getSourceId()).isEqualTo("source-id");
        assertThat(foundActivity.get().getActivityType()).isEqualTo(POST_ACTIVITY);
    }

    @Test
    void givenSavedActivity_whenDeleted_thenNotFound() {
        //given
        ActivityEntity activity = new ActivityEntity("user-id", "source-id", POST_ACTIVITY);
        this.activityRepository.save(activity);

        //when
        ActivityId id = new ActivityId("user-id", "source-id", POST_ACTIVITY);
        this.activityRepository.deleteById(id);
        Optional<ActivityEntity> foundActivity = this.activityRepository.findById(id);

        //then
        assertThat(foundActivity).isEmpty();
    }
}