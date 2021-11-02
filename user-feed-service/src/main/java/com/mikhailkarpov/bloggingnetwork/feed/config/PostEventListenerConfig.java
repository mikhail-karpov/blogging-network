package com.mikhailkarpov.bloggingnetwork.feed.config;

import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEventListener;
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

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.messaging.posts")
@Getter
@Setter
public class PostEventListenerConfig {

    @NotBlank
    private String topicExchange;

    @NotBlank
    private String postEventQueue;

    @NotBlank
    private String postCreatedRoutingKey;

    @NotBlank
    private String postDeletedRoutingKey;

    @Bean
    public TopicExchange postTopicExchange() {
        return new TopicExchange(this.topicExchange);
    }

    @Bean
    public Queue postEventQueue() {
        return new Queue(this.postEventQueue);
    }

    @Bean
    public Binding postCreatedBinding(TopicExchange postTopicExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postTopicExchange)
                .with(this.postCreatedRoutingKey);
    }

    @Bean
    public Binding postDeletedBinding(TopicExchange postTopicExchange, Queue postEventQueue) {
        return BindingBuilder
                .bind(postEventQueue)
                .to(postTopicExchange)
                .with(this.postDeletedRoutingKey);
    }

    @Bean
    public PostEventListener postEventListener(ActivityService<PostActivity> activityService) {
        return new PostEventListener(activityService);
    }
}
