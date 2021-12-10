package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingQueueBindingConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowingRabbitListener {

    public static final String LISTENER_ID = "following-listener";

    private final UserFeedService userFeedService;

    @RabbitListener(id = LISTENER_ID, queues = FollowingQueueBindingConfig.FOLLOWING_EVENT_QUEUE)
    public void handle(FollowingMessage message) {

        String followerUserId = message.getFollowerUserId();
        String followingUserId = message.getFollowingUserId();

        switch (message.getStatus()) {
            case FOLLOWED:
                this.userFeedService.startFollowing(followerUserId, followingUserId);
                break;
            case UNFOLLOWED:
                this.userFeedService.stopFollowing(followerUserId, followingUserId);
                break;
        }
    }
}
