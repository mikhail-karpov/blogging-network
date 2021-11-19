package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowingActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private FollowingActivityServiceImpl followingActivityService;

    @Captor
    private ArgumentCaptor<ActivityEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<ActivityId> idCaptor;

    @Test
    void givenActivity_whenSaved_thenFound() {
        //given
        FollowingActivity activity = new FollowingActivity("follower", "user");

        //when
        this.followingActivityService.save(activity);

        //then
        verify(this.activityRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getUserId()).isEqualTo("follower");
        assertThat(entityCaptor.getValue().getSourceId()).isEqualTo("user");
        assertThat(entityCaptor.getValue().getActivityType()).isEqualTo(FOLLOWING_ACTIVITY);
    }

    @Test
    void givenActivity_whenSavedAndDeleted_thenDeleted() {
        //given
        FollowingActivity activity = new FollowingActivity("follower", "user");

        //when
        this.followingActivityService.save(activity);
        this.followingActivityService.delete(activity);

        //then
        verify(this.activityRepository).deleteById(idCaptor.capture());
        assertThat(idCaptor.getValue().getUserId()).isEqualTo("follower");
        assertThat(idCaptor.getValue().getSourceId()).isEqualTo("user");
        assertThat(idCaptor.getValue().getType()).isEqualTo(FOLLOWING_ACTIVITY);
    }
}