package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import org.assertj.core.api.Assertions;
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

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEventListener.LISTENER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov.blogging-network:post-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class PostEventListenerIT extends AbstractRabbitMQContainer {

    @TestConfiguration
    @RabbitListenerTest
    public static class RabbitListenerConfig {

        @Bean
        public PostEventListener postEventListener(ActivityService<PostActivity> activityService) {
            return new PostEventListener(activityService);
        }
    }

    @Autowired
    private StubTrigger stubTrigger;

    @Autowired
    private RabbitListenerTestHarness harness;

    @MockBean
    private ActivityService<PostActivity> postActivityService;

    @Captor
    private ArgumentCaptor<PostActivity> activityArgumentCaptor;

    @Test
    void givenPostCreated_thenPostActivityIsSaved() throws InterruptedException {
        //given
        PostEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("post.created");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.postActivityService).saveActivity(this.activityArgumentCaptor.capture());

        PostActivity activity = this.activityArgumentCaptor.getValue();
        assertThat(activity.getPostId()).isNotNull();
        assertThat(activity.getPostAuthorId()).isNotNull();
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.POST_ACTIVITY);
    }

    @Test
    void givenPostDeleted_thenPostActivityIsDeleted() throws InterruptedException {
        //given
        PostEventListener eventListener = this.harness.getSpy(LISTENER_ID);
        assertThat(eventListener).isNotNull();

        LatchCountDownAndCallRealMethodAnswer answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(answer).when(eventListener).handle(any());

        //when
        this.stubTrigger.trigger("post.deleted");

        //then
        assertThat(answer.await(30)).isTrue();
        verify(this.postActivityService).deleteActivity(this.activityArgumentCaptor.capture());

        PostActivity activity = this.activityArgumentCaptor.getValue();
        assertThat(activity.getPostId()).isNotNull();
        assertThat(activity.getPostAuthorId()).isNotNull();
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.POST_ACTIVITY);
    }
}
