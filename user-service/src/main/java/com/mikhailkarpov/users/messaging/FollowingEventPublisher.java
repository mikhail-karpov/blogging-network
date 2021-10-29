package com.mikhailkarpov.users.messaging;

public interface FollowingEventPublisher {

    void publish(FollowingEvent event);
}
