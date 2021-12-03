package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {

    UUID createComment(UUID postId, String userId, String comment);

    Page<CommentDto> findAllByPostId(UUID postId, Pageable pageable);

    CommentDto findById(UUID commentId);

    void deleteComment(UUID commentId);

}
