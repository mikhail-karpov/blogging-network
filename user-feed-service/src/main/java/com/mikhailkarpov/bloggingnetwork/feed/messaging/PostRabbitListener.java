package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostQueueBindingConfig;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostRabbitListener {

    public static final String LISTENER_ID = "post-listener";

    private final UserFeedService userFeedService;

    @RabbitListener(id = LISTENER_ID, queues = PostQueueBindingConfig.POST_EVENT_QUEUE)
    public void handle(PostMessage message) {

        final String creatorId = message.getAuthorId();
        final String postId = message.getPostId();

        switch (message.getStatus()) {
            case CREATED:
                this.userFeedService.addPost(creatorId, postId);
                break;
            case DELETED:
                this.userFeedService.removePost(creatorId, postId);
                break;
        }
    }
}
