package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.UNFOLLOWED;

@Slf4j
@RequiredArgsConstructor
public class FollowingEventListener {

    public static final String LISTENER_ID = "following-event-listener";

    private final ActivityService<FollowingActivity> followingActivityService;

    @RabbitListener(id = LISTENER_ID, queues = {"${app.messaging.users.following-event-queue}"})
    void handle(FollowingEvent message) {

        log.info("Received {}", message);

        FollowingActivity activity = new FollowingActivity(message.getFollowerUserId(), message.getFollowingUserId());
        FollowingEvent.Status status = message.getStatus();

        if (FOLLOWED == status) {
            this.followingActivityService.saveActivity(activity);

        } else if (UNFOLLOWED == status) {
            this.followingActivityService.deleteActivity(activity);

        } else {
            throw new IllegalStateException("Unexpected message status: " + status);
        }
    }
}
