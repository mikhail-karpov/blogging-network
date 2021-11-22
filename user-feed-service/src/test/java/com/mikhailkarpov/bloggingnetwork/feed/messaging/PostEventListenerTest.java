package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.AmqpConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
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

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEventListener.LISTENER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        AmqpConfig.class,
        PostEventListenerTest.RabbitListenerConfig.class,
        PostEventListenerConfig.class})
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov.blogging-network:post-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=true",
        "spring.main.allow-bean-definition-overriding=true"})
public class PostEventListenerTest {

    @TestConfiguration
    @RabbitListenerTest
    public static class RabbitListenerConfig {

        @Bean
        public PostEventListener postEventListener(ActivityService activityService) {
            return new PostEventListener(activityService);
        }
    }

    @Autowired
    private StubTrigger stubTrigger;

    @Autowired
    private RabbitListenerTestHarness harness;

    @MockBean
    private ActivityService activityService;

    @Captor
    private ArgumentCaptor<PostActivity> activityArgumentCaptor;

    @Captor
    private ArgumentCaptor<ActivityId> idArgumentCaptor;

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
        verify(this.activityService).save(this.activityArgumentCaptor.capture());
        assertThat(this.activityArgumentCaptor.getValue().getAuthorId()).isEqualTo("author-id");
        assertThat(this.activityArgumentCaptor.getValue().getPostId()).isEqualTo("post-id");
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
        verify(this.activityService).deleteById(this.idArgumentCaptor.capture());
        assertThat(this.idArgumentCaptor.getValue().getUserId()).isEqualTo("author-id");
        assertThat(this.idArgumentCaptor.getValue().getSourceId()).isEqualTo("post-id");
        assertThat(this.idArgumentCaptor.getValue().getActivityType()).isEqualTo(ActivityType.POST_ACTIVITY);
    }
}
