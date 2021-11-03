package com.mikhailkarpov.bloggingnetwork.posts.config.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.AmqpPostEventPublisher;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEventPublisher;
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
import org.springframework.security.core.Authentication;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMQConfig {

    @Autowired
    private MessagingProperties properties;

    @Bean
    public TopicExchange postExchange() {
        String topicExchange = this.properties.getTopicExchange();
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Queue postEventQueue() {
        String followingEventQueue = this.properties.getPostEventQueue();
        return new Queue(followingEventQueue);
    }

    @Bean
    public Binding postCreatedEventBinding(TopicExchange postExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postExchange)
                .with(this.properties.getPostCreatedRoutingKey());

    }

    @Bean
    public Binding postDeletedEventBinding(TopicExchange postExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postExchange)
                .with(this.properties.getPostDeletedRoutingKey());
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
    public PostEventPublisher postEventPublisher(RabbitTemplate rabbitTemplate) {
        return new AmqpPostEventPublisher(rabbitTemplate, this.properties);
    }
}
