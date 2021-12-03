package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.CommentProjection;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends PagingAndSortingRepository<Comment, UUID> {

    Page<CommentProjection> findAllByPostId(UUID postId, Pageable pageable);

    Optional<CommentProjection> findCommentById(UUID commentId);
}
