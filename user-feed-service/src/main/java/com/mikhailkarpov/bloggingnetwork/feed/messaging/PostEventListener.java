package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.Activity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.CREATED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.DELETED;

@Slf4j
@RequiredArgsConstructor
public class PostEventListener {

    public static final String LISTENER_ID = "post-event-listener";

    private final ActivityService activityService;

    @RabbitListener(id = LISTENER_ID, queues = PostEventListenerConfig.POST_EVENT_QUEUE)
    void handle(PostEvent event) {

        String authorId = event.getAuthorId();
        String postId = event.getPostId();
        PostEvent.Status status = event.getStatus();

        if (CREATED == status) {
            Activity activity = new PostActivity(postId, authorId);
            this.activityService.save(activity);

        } else if (DELETED == status) {
            ActivityId id = new ActivityId(authorId, postId, ActivityType.POST_ACTIVITY);
            this.activityService.deleteById(id);

        } else {
            throw new IllegalStateException("Unexpected status: " + status);
        }
    }
}
