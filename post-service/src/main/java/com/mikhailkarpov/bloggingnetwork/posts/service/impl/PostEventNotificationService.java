package com.mikhailkarpov.bloggingnetwork.posts.service.impl;

import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.PostEvent;
import lombok.NonNull;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class PostEventNotificationService extends StreamNotificationService<PostEvent> {

    public PostEventNotificationService(@NonNull StreamBridge streamBridge) {
        super(streamBridge);
    }

    @Override
    protected String getBinding() {
        return "postEvents-out-0";
    }
}
