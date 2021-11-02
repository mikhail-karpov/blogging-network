package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEventListener.LISTENER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov:user-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
class FollowingEventListenerIT extends AbstractRabbitMQContainer {

    @TestConfiguration
    @RabbitListenerTest
    public static class RabbitListenerConfig {

        @Bean
        public FollowingEventListener followingEventListener(ActivityService<FollowingActivity> activityService) {
            return new FollowingEventListener(activityService);
        }
    }

    @Autowired
    private StubTrigger stubTrigger;

    @Autowired
    private RabbitListenerTestHarness harness;

    @MockBean
    private ActivityService<FollowingActivity> activityService;

    @Captor
    private ArgumentCaptor<FollowingActivity> followingActivityArgumentCaptor;

    @Test
    void givenFollowingEvent_thenFollowingActivityIsSaved() throws InterruptedException {
        //given
        FollowingEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("user.follows.event");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.activityService).saveActivity(this.followingActivityArgumentCaptor.capture());

        FollowingActivity activity = followingActivityArgumentCaptor.getValue();
        assertThat(activity.getFollowerUserId()).isNotNull();
        assertThat(activity.getFollowingUserId()).isNotNull();
        assertThat(activity.getActivityType()).isEqualTo(FOLLOWING_ACTIVITY);
    }

    @Test
    void givenUnfollowingEvent_thenFollowingActivityIsDeleted() throws InterruptedException {
        //given
        FollowingEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("user.unfollows.event");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.activityService).deleteActivity(this.followingActivityArgumentCaptor.capture());

        FollowingActivity activity = followingActivityArgumentCaptor.getValue();
        assertThat(activity.getFollowerUserId()).isNotNull();
        assertThat(activity.getFollowingUserId()).isNotNull();
        assertThat(activity.getActivityType()).isEqualTo(FOLLOWING_ACTIVITY);
    }
}