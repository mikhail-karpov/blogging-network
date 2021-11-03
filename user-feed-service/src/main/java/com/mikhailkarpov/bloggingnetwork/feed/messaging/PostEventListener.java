package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.CREATED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.DELETED;

@Slf4j
@RequiredArgsConstructor
public class PostEventListener {

    public static final String LISTENER_ID = "post-event-listener";

    private final PostActivityService activityService;

    @RabbitListener(id = LISTENER_ID, queues = PostEventListenerConfig.POST_EVENT_QUEUE)
    void handle(PostEvent event) {

        log.info("Received {}", event);
        PostEvent.Status status = event.getStatus();

        if (CREATED == status) {
            this.activityService.save(event);

        } else if (DELETED == status) {
            this.activityService.delete(event);

        } else {
            throw new IllegalStateException("Unexpected status: " + status);
        }
    }
}
