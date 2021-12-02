package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CommentService {

    UUID createComment(UUID postId, String userId, String comment);

    Page<Comment> findAllByPostId(UUID postId, Pageable pageable);

    Optional<Comment> findById(UUID commentId);

    void removeComment(UUID postId, UUID commentId);

}
