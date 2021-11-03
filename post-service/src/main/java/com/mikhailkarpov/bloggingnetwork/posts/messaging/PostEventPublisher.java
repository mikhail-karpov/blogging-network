package com.mikhailkarpov.bloggingnetwork.posts.messaging;

public interface PostEventPublisher {

    void publish(PostEvent event);
}
