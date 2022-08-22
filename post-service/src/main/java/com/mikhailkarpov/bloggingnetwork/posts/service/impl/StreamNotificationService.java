package com.mikhailkarpov.bloggingnetwork.posts.service.impl;

import com.mikhailkarpov.bloggingnetwork.posts.dto.notification.Notification;
import com.mikhailkarpov.bloggingnetwork.posts.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@RequiredArgsConstructor
public abstract class StreamNotificationService<T extends Notification> implements NotificationService<T> {

    private final StreamBridge streamBridge;

    @Override
    public final void send(T t) {
        Message<T> message = MessageBuilder
                .withPayload(t)
                .setHeader("type", t.getType())
                .build();

        String binding = getBinding();

        log.info("Sending {} to {}", t, binding);
        streamBridge.send(binding, message);
    }

    protected abstract String getBinding();
}
