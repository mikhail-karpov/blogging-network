package com.mikhailkarpov.bloggingnetwork.feed.domain;

public class PostActivity extends AbstractActivity {

    public PostActivity(String postAuthorId, String postId) {
        super(postAuthorId, postId, ActivityType.POST_ACTIVITY);
    }

    public String getPostAuthorId() {
        return this.userId;
    }

    public String getPostId() {
        return this.sourceId;
    }
}
