package com.mikhailkarpov.bloggingnetwork.feed.config;

import com.mikhailkarpov.bloggingnetwork.feed.domain.FollowingActivity;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEventListener;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Configuration
@Validated
@ConfigurationProperties(prefix = "app.messaging.users")
@Getter
@Setter
public class FollowingEventListenerConfig {

    @NotBlank
    private String topicExchange;

    @NotBlank
    public static String followingEventQueue;

    @NotBlank
    private String followRoutingKey;

    @NotBlank
    private String unfollowRoutingKey;

    @Bean
    public TopicExchange usersTopicExchange() {
        return new TopicExchange(this.topicExchange);
    }

    @Bean
    public Queue followingEventQueue() {
        return new Queue(this.followingEventQueue);
    }

    @Bean
    public Binding followEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(this.followRoutingKey);
    }

    @Bean
    public Binding unfollowEventBinding(TopicExchange usersTopicExchange, Queue followingEventQueue) {
        return BindingBuilder
                .bind(followingEventQueue)
                .to(usersTopicExchange)
                .with(this.unfollowRoutingKey);
    }

    @Bean
    public FollowingEventListener followingEventListener(ActivityService<FollowingActivity> activityService) {
        return new FollowingEventListener(activityService);
    }
}
