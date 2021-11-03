package com.mikhailkarpov.bloggingnetwork.posts.config.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.messaging.posts")
@Getter
@Setter
public class MessagingProperties {

    @NotBlank
    private String topicExchange;

    @NotBlank
    private String postEventQueue;

    @NotBlank
    private String postCreatedRoutingKey;

    @NotBlank
    private String postDeletedRoutingKey;
}
