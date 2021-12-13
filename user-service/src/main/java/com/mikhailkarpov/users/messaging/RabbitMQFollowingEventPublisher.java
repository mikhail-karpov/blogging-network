package com.mikhailkarpov.users.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class RabbitMQFollowingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String followEventRoutingKey;
    private final String unfollowEventRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(FollowingEvent event) {

        String routingKey = null;

        if (FollowingEvent.Status.FOLLOWED == event.getStatus()) {
            routingKey = followEventRoutingKey;

        } else if (FollowingEvent.Status.UNFOLLOWED == event.getStatus()) {
            routingKey = unfollowEventRoutingKey;
        }

        log.info("Sending message: {}", event);
        this.rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
