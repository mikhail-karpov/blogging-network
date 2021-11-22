package com.mikhailkarpov.bloggingnetwork.feed.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("POST_ACTIVITY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostActivity extends Activity {

    public PostActivity(String postId, String authorId) {
        super(authorId, postId, ActivityType.POST_ACTIVITY);
    }

    public String getAuthorId() {
        return getUserId();
    }

    public String getPostId() {
        return getSourceId();
    }

}
