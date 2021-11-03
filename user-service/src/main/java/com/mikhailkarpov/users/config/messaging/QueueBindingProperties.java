package com.mikhailkarpov.users.config.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.messaging.users")
@Getter
@Setter
public class QueueBindingProperties {

    @NotBlank
    private String topicExchange;

    @NotBlank
    private String followingEventQueue;

    @NotBlank
    private String followRoutingKey;

    @NotBlank
    private String unfollowRoutingKey;
}
