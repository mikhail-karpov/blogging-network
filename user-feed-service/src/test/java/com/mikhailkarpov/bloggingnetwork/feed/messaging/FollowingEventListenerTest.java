package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.AmqpConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEventListener.LISTENER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        AmqpConfig.class,
        FollowingEventListenerTest.RabbitListenerConfig.class,
        FollowingEventListenerConfig.class})
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov:user-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=true",
        "spring.main.allow-bean-definition-overriding=true"})
class FollowingEventListenerTest {

    @TestConfiguration
    @RabbitListenerTest
    public static class RabbitListenerConfig {

        @Bean
        public FollowingEventListener followingEventListener(ActivityService activityService) {
            return new FollowingEventListener(activityService);
        }
    }

    @Autowired
    private StubTrigger stubTrigger;

    @Autowired
    private RabbitListenerTestHarness harness;

    @MockBean
    private ActivityService activityService;

    @Captor
    private ArgumentCaptor<FollowingActivity> activityArgumentCaptor;

    @Captor
    private ArgumentCaptor<ActivityId> idArgumentCaptor;

    @Test
    void givenUserFollows_thenFollowingActivityIsSaved() throws InterruptedException {
        //given
        FollowingEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("user.follows.event");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.activityService).save(this.activityArgumentCaptor.capture());
        assertThat(this.activityArgumentCaptor.getValue().getFollowerUserId()).isEqualTo("followerId");
        assertThat(this.activityArgumentCaptor.getValue().getFollowingUserId()).isEqualTo("followingId");
    }

    @Test
    void givenFollowingEvent_thenFollowingActivityIsSaved() throws InterruptedException {
        //given
        FollowingEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("user.unfollows.event");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.activityService).deleteById(this.idArgumentCaptor.capture());
        assertThat(this.idArgumentCaptor.getValue().getUserId()).isEqualTo("followerId");
        assertThat(this.idArgumentCaptor.getValue().getSourceId()).isEqualTo("followingId");
        assertThat(this.idArgumentCaptor.getValue().getActivityType()).isEqualTo(ActivityType.FOLLOWING_ACTIVITY);
    }
}