package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.AmqpConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostQueueBindingConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostRabbitListener.LISTENER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@AutoConfigureStubRunner(
        ids = "com.mikhailkarpov.blogging-network:post-service",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ContextConfiguration(classes = {
        RabbitAutoConfiguration.class,
        AmqpConfig.class,
        PostQueueBindingConfig.class,
        PostRabbitListenerTest.PostEventRabbitListenerTestConfig.class
})
@TestPropertySource(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false",
        "spring.main.allow-bean-definition-overriding=true"
})
@Testcontainers
class PostRabbitListenerTest {

    @Container
    static RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq").withExposedPorts(5672);

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT_MQ::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @TestConfiguration
    @RabbitListenerTest
    public static class PostEventRabbitListenerTestConfig {

        @Bean
        public PostRabbitListener postEventRabbitListener(UserFeedService userFeedService) {
            return new PostRabbitListener(userFeedService);
        }
    }

    @MockBean
    private UserFeedService userFeedService;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Autowired
    private StubTrigger stubTrigger;

    @Captor
    private ArgumentCaptor<PostMessage> eventArgumentCaptor;

    private PostRabbitListener listener;
    private LatchCountDownAndCallRealMethodAnswer answer;

    @BeforeEach
    void setUp() {
        this.listener = this.harness.getSpy(LISTENER_ID);
        assertNotNull(this.listener);

        this.answer = this.harness.getLatchAnswerFor(LISTENER_ID, 1);
        doAnswer(this.answer).when(this.listener).handle(any());
    }

    @Test
    void givenPostCreated_thenAddPost() throws InterruptedException {
        this.stubTrigger.trigger("post.created");

        assertTrue(this.answer.await(30));
        verify(this.userFeedService).addPost("author-id", "post-id");
    }

    @Test
    void givenPostDeleted_thenRemovePost() throws InterruptedException {
        this.stubTrigger.trigger("post.deleted");

        assertTrue(this.answer.await(30));
        verify(this.userFeedService).removePost("author-id", "post-id");
    }
}