package com.mikhailkarpov.bloggingnetwork.posts.service.impl;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Comment;
import com.mikhailkarpov.bloggingnetwork.posts.domain.CommentProjection;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.CommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import com.mikhailkarpov.bloggingnetwork.posts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Override
    @Transactional
    public UUID createComment(UUID postId, String userId, String comment) {

        Post post = this.postRepository.findById(postId).orElseThrow(() -> {
            String message = String.format("Post with id='%s' not found", postId);
            return new ResourceNotFoundException(message);
        });

        Comment savedComment = this.commentRepository.save(new Comment(post, userId, comment));
        return savedComment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> findAllByPostId(UUID postId, Pageable pageable) {

        return this.commentRepository.findAllByPostId(postId, pageable).map(this::mapFromProjection);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto findById(UUID commentId) {

        return this.commentRepository.findCommentById(commentId).map(this::mapFromProjection).orElseThrow(() -> {
            String message = String.format("Comment with id=%s not found", commentId);
            return new ResourceNotFoundException(message);
        });
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {

        if (!this.commentRepository.existsById(commentId)) {
            String message = String.format("Post comment with id=%s not found", commentId);
            throw new ResourceNotFoundException(message);
        }

        this.commentRepository.deleteById(commentId);
    }

    private CommentDto mapFromProjection(CommentProjection commentProjection) {
        return CommentDto.builder()
                .id(commentProjection.getId().toString())
                .user(this.userService.getUserById(commentProjection.getUserId()))
                .comment(commentProjection.getComment())
                .createdDate(commentProjection.getCreatedDate())
                .build();
    }
}
