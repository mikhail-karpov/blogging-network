package com.mikhailkarpov.bloggingnetwork.feed.event;

import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class FollowingEventConsumer implements Consumer<FollowingEvent> {

    private final UserFeedService userFeedService;

    @Override
    public void accept(@NonNull FollowingEvent event) {
        String followerUserId = event.getFollowerUserId();
        String followingUserId = event.getFollowingUserId();

        switch (event.getStatus()) {
            case FOLLOWED:
                this.userFeedService.startFollowing(followerUserId, followingUserId);
                break;
            case UNFOLLOWED:
                this.userFeedService.stopFollowing(followerUserId, followingUserId);
                break;
        }

    }
}
