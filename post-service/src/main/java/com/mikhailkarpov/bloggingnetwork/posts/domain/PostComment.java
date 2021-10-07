package com.mikhailkarpov.bloggingnetwork.posts.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends AuthorEntity {

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_fk")
    private Post post;

    public PostComment(String userId, String content) {
        super(userId);
        this.content = content;
    }

    void setPost(Post post) {
        this.post = post;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + getId() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", content='" + content + '\'' +
                ", post=" + post +
                ", createdDate=" + getCreatedDate() +
                '}';
    }
}
