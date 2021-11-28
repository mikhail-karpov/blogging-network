package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.config.RabbitListenerTestConfig;
import com.mikhailkarpov.users.config.RabbitListenerTestConfig.TestListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(classes = RabbitListenerTestConfig.class)
public class FollowingServiceMessagingIT extends AbstractIT {

    @Autowired
    private FollowingService followingService;

    @Autowired
    private RabbitListenerTestHarness harness;

    private TestListener listener;
    private LatchCountDownAndCallRealMethodAnswer answer;

    @BeforeEach
    void setUp() {
        this.listener = this.harness.getSpy(TestListener.LISTENER_ID);
        assertThat(this.listener).isNotNull();

        answer = this.harness.getLatchAnswerFor(TestListener.LISTENER_ID, 1);
        doAnswer(answer).when(listener).handle(any());
    }

    @AfterEach
    void awaitForEvent() throws InterruptedException {
        assertThat(answer.await(30)).isTrue();
        verify(listener).handle(any());
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
    @Sql(scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void whenAddToFollowers_thenEventSent() {
        this.followingService.addToFollowers("1", "3");
    }

    @Test
    @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
    @Sql(scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void whenRemoveFromFollowers_thenEventSent() {
        this.followingService.removeFromFollowers("3", "1");
    }
}
