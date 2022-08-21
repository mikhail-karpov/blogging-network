package com.mikhailkarpov.users.service.impl.notification;

import com.mikhailkarpov.users.dto.FollowingNotification;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowingNotificationService extends AbstractNotificationService<FollowingNotification> {

    private static final String BINDING = "userFollowing-out-0";

    public FollowingNotificationService(@NonNull StreamBridge streamBridge) {
        super(streamBridge);
    }

    @Override
    protected String getBindingName() {
        return BINDING;
    }

}
