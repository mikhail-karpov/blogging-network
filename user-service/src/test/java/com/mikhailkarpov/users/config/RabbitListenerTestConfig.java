package com.mikhailkarpov.users.config;

import com.mikhailkarpov.users.messaging.FollowingEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Component;

@TestConfiguration
@RabbitListenerTest
public class RabbitListenerTestConfig {

    @Component
    public class TestListener {

        public static final String LISTENER_ID = MessagingConfig.QUEUE;

        @RabbitListener(id = LISTENER_ID, queues = MessagingConfig.QUEUE)
        public void handle(FollowingEvent event) {
            //do nothing
        }
    }
}
