package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.AmqpConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingQueueBindingConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingRabbitListener.LISTENER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov:user-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        AmqpConfig.class,
        FollowingQueueBindingConfig.class,
        FollowingRabbitListenerTest.FollowingEventRabbitListenerTestConfig.class
})
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false",
        "spring.main.allow-bean-definition-overriding=true"
})
@Testcontainers
class FollowingRabbitListenerTest {

    @Container
    static RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT_MQ::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @TestConfiguration
    @RabbitListenerTest
    public static class FollowingEventRabbitListenerTestConfig {

        @Bean
        public FollowingRabbitListener followingEventRabbitListener(UserFeedService userFeedService) {
            return new FollowingRabbitListener(userFeedService);
        }
    }

    @MockBean
    private UserFeedService userFeedService;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Autowired
    private StubTrigger stubTrigger;

    private FollowingRabbitListener listener;
    private LatchCountDownAndCallRealMethodAnswer answer;

    @BeforeEach
    void setUp() {
        this.listener = this.harness.getSpy(LISTENER_ID);
        assertNotNull(this.listener);

        this.answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(this.answer).when(this.listener).handle(any());
    }

    @Test
    void givenFollowingMessage_thenStartFollowing() throws InterruptedException {
        this.stubTrigger.trigger("user.follows.event");

        assertTrue(this.answer.await(30));
        verify(this.userFeedService).startFollowing("followerId", "followingId");
    }

        @Test
    void givenUnfollowingMessage_thenStopFollowing() throws InterruptedException {
        this.stubTrigger.trigger("user.unfollows.event");

        assertTrue(this.answer.await(30));
        verify(this.userFeedService).stopFollowing("followerId", "followingId");
    }
}