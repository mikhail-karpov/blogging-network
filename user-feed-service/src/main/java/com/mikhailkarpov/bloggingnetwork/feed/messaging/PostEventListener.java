package com.mikhailkarpov.bloggingnetwork.feed.messaging;

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

    private final ActivityService<PostActivity> postActivityService;

    @RabbitListener(id = LISTENER_ID, queues = "${app.messaging.posts.post-event-queue}")
    void handle(PostEvent event) {

        log.info("Received {}", event);
        PostActivity activity = new PostActivity(event.getAuthorId(), event.getPostId());
        PostEvent.Status status = event.getStatus();

        if (CREATED == status) {
            this.postActivityService.saveActivity(activity);

        } else if (DELETED == status) {
            this.postActivityService.deleteActivity(activity);

        } else {
            throw new IllegalStateException("Unexpected status: " + status);
        }
    }
}
