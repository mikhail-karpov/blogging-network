package com.mikhailkarpov.users.domain;

import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

@Component
@RequiredArgsConstructor
public class FollowingEntityListener {

    private final FollowingEventPublisher eventPublisher;

    @PostPersist
    private void sendFollowedEvent(Following following) {
        String followerUserId = following.getFollowerUserId();
        String followingUserId = following.getFollowingUserId();
        FollowingEvent.Status status = FollowingEvent.Status.FOLLOWED;

        FollowingEvent event = new FollowingEvent(followerUserId, followingUserId, status);
        this.eventPublisher.publish(event);
    }

    @PostRemove
    private void sendUnfollowedEvent(Following following) {
        String followerUserId = following.getFollowerUserId();
        String followingUserId = following.getFollowingUserId();
        FollowingEvent.Status status = FollowingEvent.Status.UNFOLLOWED;

        FollowingEvent event = new FollowingEvent(followerUserId, followingUserId, status);
        this.eventPublisher.publish(event);
    }
}
