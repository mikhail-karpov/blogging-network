package com.mikhailkarpov.bloggingnetwork.feed.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter(objectMapper);
        return messageConverter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(this.connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(3);

        return factory;
    }
}
