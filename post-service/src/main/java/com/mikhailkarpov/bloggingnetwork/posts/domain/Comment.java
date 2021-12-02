package com.mikhailkarpov.bloggingnetwork.posts.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "post_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Column(name = "comment", nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_fk")
    private Post post;

    public Comment(String userId, String comment) {
        super(userId);
        this.comment = comment;
        this.post = null;
    }

    public Comment(Post post, String userId, String comment) {
        super(userId);
        this.comment = comment;
        setPost(post);
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + getId() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", content='" + comment + '\'' +
                ", post=" + post +
                ", createdDate=" + getCreatedDate() +
                '}';
    }
}
