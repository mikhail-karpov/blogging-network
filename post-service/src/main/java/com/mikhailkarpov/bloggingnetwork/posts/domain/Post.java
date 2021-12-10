package com.mikhailkarpov.bloggingnetwork.posts.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public class Post extends BaseEntity {

    @Column(name = "content", nullable = false)
    private String content;

    @OneToMany(fetch = LAZY, mappedBy = "post", cascade = {ALL}, orphanRemoval = true)
    private final Set<Comment> comments = new HashSet<>();

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
