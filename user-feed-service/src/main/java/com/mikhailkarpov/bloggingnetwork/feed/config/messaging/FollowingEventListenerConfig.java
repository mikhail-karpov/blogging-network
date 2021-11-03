package com.mikhailkarpov.bloggingnetwork.feed.config.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEventListener;
import com.mikhailkarpov.bloggingnetwork.feed.services.FollowingActivityService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FollowingEventListenerConfig {

    public static final String TOPIC_EXCHANGE = "users";
    public static final String FOLLOWING_EVENT_QUEUE = "following-event-queue";
    public static final String FOLLOW_ROUTING_KEY = "user.follow";
    public static final String UNFOLLOW_ROUTING_KEY = "user.unfollow";

    @Bean
    public TopicExchange usersTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue followingEventQueue() {
        return new Queue(FOLLOWING_EVENT_QUEUE);
    }

    @Bean
    public Binding followEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(FOLLOW_ROUTING_KEY);
    }

    @Bean
    public Binding unfollowEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(UNFOLLOW_ROUTING_KEY);
    }

    @Bean
    public FollowingEventListener followingEventListener(FollowingActivityService activityService) {
        return new FollowingEventListener(activityService);
    }
}
