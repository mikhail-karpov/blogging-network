package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.CommentProjection;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface CommentRepository extends PagingAndSortingRepository<Comment, UUID> {

    Page<Comment> findAllByPostId(UUID postId, Pageable pageable);

    Page<CommentProjection> findCommentsByPostId(UUID postId, Pageable pageable);
}
