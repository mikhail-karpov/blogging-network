package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.config.messaging.MessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class AmqpPostEventPublisher implements PostEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    @Override
    public void publish(PostEvent event) {

        String exchange = properties.getTopicExchange();
        String routingKey;

        if (EventStatus.CREATED == event.getStatus()) {
            routingKey = properties.getPostCreatedRoutingKey();

        } else {
            routingKey = properties.getPostDeletedRoutingKey();
        }

        log.info("Sending {}", event);
        this.rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
