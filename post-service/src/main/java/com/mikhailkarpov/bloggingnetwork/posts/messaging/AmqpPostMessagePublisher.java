package com.mikhailkarpov.bloggingnetwork.posts.messaging;

import com.mikhailkarpov.bloggingnetwork.posts.event.PostAbstractEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class AmqpPostMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String postCreatedRoutingKey;
    private final String postDeletedRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(PostAbstractEvent event) {

        PostMessage message = new PostMessage(event.getPostId(), event.getAuthorId(), event.getStatus());

        switch (event.getStatus()) {
            case CREATED:
                this.rabbitTemplate.convertAndSend(exchange, postCreatedRoutingKey, message);
                break;
            case DELETED:
                this.rabbitTemplate.convertAndSend(exchange, postDeletedRoutingKey, message);
                break;
        }

        log.info("Sent: {}", message);
    }
}
