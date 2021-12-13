package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class AmqpPostEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String postCreatedRoutingKey;
    private final String postDeletedRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(PostEvent event) {

        switch (event.getStatus()) {
            case CREATED:
                this.rabbitTemplate.convertAndSend(exchange, postCreatedRoutingKey, event);
                break;
            case DELETED:
                this.rabbitTemplate.convertAndSend(exchange, postDeletedRoutingKey, event);
                break;
        }

        log.info("Sent: {}", event);
    }
}
