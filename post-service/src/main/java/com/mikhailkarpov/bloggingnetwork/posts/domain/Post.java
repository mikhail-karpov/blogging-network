package com.mikhailkarpov.bloggingnetwork.posts.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.*;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public class Post extends BaseEntity {

    @Column(name = "content", nullable = false)
    private String content;

    @OneToMany(fetch = LAZY, mappedBy = "post", cascade = {ALL}, orphanRemoval = true)
    private Set<PostComment> postComments = new HashSet<>();

    public Post(String userId, String content) {
        super(userId);
        setContent(content);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Objects.requireNonNull(content);
    }

    public void addComment(PostComment postComment) {
        this.postComments.add(postComment);
        postComment.setPost(this);
    }

    public void removeComment(PostComment postComment) {
        this.postComments.remove(postComment);
        postComment.setPost(null);
    }

    public List<PostComment> getPostComments() {
        return Collections.unmodifiableList(new ArrayList<>(postComments));
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + getId() +
                ", userId='" + getUserId() + '\'' +
                ", content='" + content + '\'' +
                ", createdDate='" + getCreatedDate() + '\'' +
                '}';
    }
}
