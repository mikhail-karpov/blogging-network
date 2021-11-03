package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;

public class PostEvent extends PostActivity {

    public enum Status {
        CREATED, DELETED
    }

    private final Status status;

    public PostEvent(@JsonProperty("postId") String postId,
                     @JsonProperty("authorId") String authorId,
                     @JsonProperty("status") Status status) {
        super(postId, authorId);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PostEvent event = (PostEvent) o;

        return super.equals(o) && status == event.status;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PostEvent{" +
                "postId=" + getPostId() +
                ", authorId=" + getAuthorId() +
                "status=" + status +
                '}';
    }
}
