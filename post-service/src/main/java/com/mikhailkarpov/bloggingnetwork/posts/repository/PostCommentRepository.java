package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostCommentRepository {

    void deleteById(UUID commentId);

    Page<PostComment> findAllByPostId(UUID postId, Pageable pageable);

    Optional<PostComment> findById(UUID commentId);

}
