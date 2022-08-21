package com.mikhailkarpov.users.service.impl.notification;

import com.mikhailkarpov.users.dto.Notification;
import com.mikhailkarpov.users.service.NotificationService;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractNotificationService<T extends Notification> implements NotificationService<T> {

    @NonNull StreamBridge streamBridge;

    @Override
    public final void send(@NonNull T notification) {
        MessageBuilder<T> messageBuilder = MessageBuilder.withPayload(notification);
        getHeaders().forEach((key, value) -> messageBuilder.setHeader(key, value));
        messageBuilder.setHeader("type", notification.getType());

        Message<T> message = messageBuilder.build();
        String bindingName = getBindingName();

        streamBridge.send(bindingName, message);
        log.info("Sending {} to {}", message.getPayload(), bindingName);
    }

    protected abstract String getBindingName();

    protected Map<String, Object> getHeaders() {
        return Collections.emptyMap();
    }

}
