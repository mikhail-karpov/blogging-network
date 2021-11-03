package com.mikhailkarpov.bloggingnetwork.feed.config.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEventListener;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostEventListenerConfig {

    public static final String TOPIC_EXCHANGE = "posts";
    public static final String POST_EVENT_QUEUE = "post-event-queue";
    public static final String POST_CREATED_ROUTING_KEY = "post.created";
    public static final String POST_DELETED_ROUTING_KEY = "post.deleted";

    @Bean
    public TopicExchange postTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue postEventQueue() {
        return new Queue(POST_EVENT_QUEUE);
    }

    @Bean
    public Binding postCreatedBinding(TopicExchange postTopicExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postTopicExchange)
                .with(POST_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding postDeletedBinding(TopicExchange postTopicExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postTopicExchange)
                .with(POST_DELETED_ROUTING_KEY);
    }

    @Bean
    public PostEventListener postEventListener(PostActivityService activityService) {
        return new PostEventListener(activityService);
    }
}
