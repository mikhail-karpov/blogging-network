package com.mikhailkarpov.bloggingnetwork.feed.event;

import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PostEventConsumer implements Consumer<PostEvent> {

    private final UserFeedService userFeedService;

    @Override
    public void accept(@NonNull PostEvent event) {

        final String creatorId = event.getAuthorId();
        final String postId = event.getPostId();

        switch (event.getStatus()) {
            case CREATED:
                this.userFeedService.addPost(creatorId, postId);
                break;
            case DELETED:
                this.userFeedService.removePost(creatorId, postId);
                break;
        }
    }
}
