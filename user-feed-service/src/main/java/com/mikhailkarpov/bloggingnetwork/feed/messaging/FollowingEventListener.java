package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
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

    private final ActivityService activityService;

    @RabbitListener(id = LISTENER_ID, queues = {FollowingEventListenerConfig.FOLLOWING_EVENT_QUEUE})
    void handle(FollowingEvent event) {

        String followerUserId = event.getFollowerUserId();
        String followingUserId = event.getFollowingUserId();
        FollowingEvent.Status status = event.getStatus();

        if (FOLLOWED == status) {
            FollowingActivity activity = new FollowingActivity(followerUserId, followingUserId);
            this.activityService.save(activity);

        } else if (UNFOLLOWED == status) {
            ActivityId id = new ActivityId(followerUserId, followingUserId, ActivityType.FOLLOWING_ACTIVITY);
            this.activityService.deleteById(id);

        } else {
            throw new IllegalStateException("Unexpected message status: " + status);
        }
    }
}
