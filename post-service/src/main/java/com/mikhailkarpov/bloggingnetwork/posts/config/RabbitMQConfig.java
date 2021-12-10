package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.AmqpPostMessagePublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    public static final String TOPIC_EXCHANGE = "posts";
    public static final String POST_EVENT_QUEUE = "post-event-queue";
    public static final String POST_CREATED_ROUTING_KEY = "post.created";
    public static final String POST_DELETED_ROUTING_KEY = "post.deleted";

    @Bean
    @Primary
    public ConnectionFactory connectionFactory(RabbitProperties rabbitProperties) {
        String host = rabbitProperties.getHost();
        Integer port = rabbitProperties.getPort();
        String username = rabbitProperties.getUsername();
        String password = rabbitProperties.getPassword();

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);

        if (username != null) {
            connectionFactory.setUsername(username);
        }

        if (password != null) {
            connectionFactory.setPassword(password);
        }

        return connectionFactory;
    }


    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue postEventQueue() {
        return new Queue(POST_EVENT_QUEUE);
    }

    @Bean
    public Binding postCreatedEventBinding(TopicExchange postExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postExchange)
                .with(POST_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding postDeletedEventBinding(TopicExchange postExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postExchange)
                .with(POST_DELETED_ROUTING_KEY);
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
    public AmqpPostMessagePublisher amqpPostMessagePublisher(RabbitTemplate rabbitTemplate) {
        return new AmqpPostMessagePublisher(
                rabbitTemplate, TOPIC_EXCHANGE, POST_CREATED_ROUTING_KEY, POST_DELETED_ROUTING_KEY);
    }
}
