package com.mikhailkarpov.users.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import com.mikhailkarpov.users.messaging.RabbitMQFollowingMessageSender;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    public static final String TOPIC_EXCHANGE = "users";
    public static final String QUEUE = "following-event-queue";
    public static final String FOLLOW_ROUTING_KEY = "user.follow";
    public static final String UNFOLLOW_ROUTING_KEY = "user.unfollow";

    @Bean
    public TopicExchange usersTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue followingEventQueue() {
        return new Queue(QUEUE);
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
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cf);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public FollowingEventPublisher followingEventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitMQFollowingMessageSender(
                rabbitTemplate, TOPIC_EXCHANGE, FOLLOW_ROUTING_KEY, UNFOLLOW_ROUTING_KEY);
    }
}
