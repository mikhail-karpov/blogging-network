package com.mikhailkarpov.users.contract;

import com.mikhailkarpov.users.api.FollowingController;
import com.mikhailkarpov.users.api.AccountController;
import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.assertTrue;

@SpringBootTest(properties = {
        "stubrunner.amqp.enabled=true",
        "stubrunner.amqp.mockConnection=false",
})
@AutoConfigureMessageVerifier
@Testcontainers
public class ContractTestBaseClass extends AbstractIT {

    @Container
    static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq");

    @DynamicPropertySource
    static void configRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
    }

    @Autowired
    private AccountController accountController;

    @Autowired
    private FollowingController followingController;

    @Autowired
    private FollowingEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(accountController, followingController);
        RestAssuredMockMvc.standaloneSetup(builder);
    }

    public void sendFollowingEvent() {
        FollowingEvent.Status eventType = FollowingEvent.Status.FOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", eventType);

        this.eventPublisher.publish(event);
    }

    public void sendUnfollowingEvent() {
        FollowingEvent.Status eventType = FollowingEvent.Status.UNFOLLOWED;
        FollowingEvent event = new FollowingEvent("followerId", "followingId", eventType);

        this.eventPublisher.publish(event);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void testConverter() {
        final MessageConverter messageConverter = this.rabbitTemplate.getMessageConverter();
        assertTrue(messageConverter instanceof Jackson2JsonMessageConverter);
    }
}
