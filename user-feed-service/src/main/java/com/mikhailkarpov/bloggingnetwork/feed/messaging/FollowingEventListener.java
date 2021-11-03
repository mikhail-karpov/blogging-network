package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.FollowingActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.UNFOLLOWED;

@Slf4j
@RequiredArgsConstructor
public class FollowingEventListener {

    public static final String LISTENER_ID = "following-event-listener";

    private final FollowingActivityService activityService;

    @RabbitListener(id = LISTENER_ID, queues = {FollowingEventListenerConfig.FOLLOWING_EVENT_QUEUE})
    void handle(FollowingEvent event) {

        log.info("Received {}", event);
        FollowingEvent.Status status = event.getStatus();

        if (FOLLOWED == status) {
            this.activityService.save(event);

        } else if (UNFOLLOWED == status) {
            this.activityService.delete(event);

        } else {
            throw new IllegalStateException("Unexpected message status: " + status);
        }
    }
}
