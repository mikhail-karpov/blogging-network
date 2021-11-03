package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class AmqpPostEventPublisher implements PostEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String postCreatedRoutingKey;
    private final String postDeletedRoutingKey;

    @Override
    public void publish(PostEvent event) {

        String routingKey;
        if (EventStatus.CREATED == event.getStatus()) {
            routingKey = postCreatedRoutingKey;

        } else {
            routingKey = postDeletedRoutingKey;
        }

        log.info("Sending {}", event);
        this.rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
