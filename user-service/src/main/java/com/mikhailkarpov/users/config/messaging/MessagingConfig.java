package com.mikhailkarpov.users.config.messaging;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(QueueBindingProperties.class)
public class MessagingConfig {

    @Autowired
    private QueueBindingProperties properties;

    @Bean
    public TopicExchange usersTopicExchange() {
        String topicExchange = this.properties.getTopicExchange();
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Queue followingEventQueue() {
        String followingEventQueue = this.properties.getFollowingEventQueue();
        return new Queue(followingEventQueue);
    }

    @Bean
    public Binding followEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(this.properties.getFollowRoutingKey());
    }

    @Bean
    public Binding unfollowEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(this.properties.getUnfollowRoutingKey());
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
    public FollowingEventPublisher followingMessageSender(RabbitTemplate rabbitTemplate) {
        String topicExchange = this.properties.getTopicExchange();
        String followRoutingKey = this.properties.getFollowRoutingKey();
        String unfollowRoutingKey = this.properties.getUnfollowRoutingKey();

        return new RabbitMQFollowingMessageSender(rabbitTemplate, topicExchange, followRoutingKey, unfollowRoutingKey);
    }
}
