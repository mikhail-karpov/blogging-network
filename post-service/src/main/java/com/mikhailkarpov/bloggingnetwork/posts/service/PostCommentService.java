package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostCommentService {

    UUID createComment(UUID postId, String userId, String comment);

    Page<PostComment> findAllByPostId(UUID postId, Pageable pageable);

    Optional<PostComment> findById(UUID commentId);

    void removeComment(UUID postId, UUID commentId);

}
