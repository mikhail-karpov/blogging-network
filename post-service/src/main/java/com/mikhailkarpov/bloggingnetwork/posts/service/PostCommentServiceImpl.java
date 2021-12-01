package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostCommentRepository;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;

    @Override
    @Transactional
    public UUID createComment(UUID postId, String userId, String comment) {

        Post post = this.postRepository.findById(postId).orElseThrow(() -> {
            String message = String.format("Post with id='%s' not found", postId);
            return new ResourceNotFoundException(message);
        });

        PostComment savedComment = this.commentRepository.save(new PostComment(post, userId, comment));
        return savedComment.getId();
    }

    @Override
    public Page<PostComment> findAllByPostId(UUID postId, Pageable pageable) {

        return this.commentRepository.findAllByPostId(postId, pageable);
    }

    @Override
    public Optional<PostComment> findById(UUID commentId) {

        return this.commentRepository.findById(commentId);
    }

    @Override
    @Transactional
    public void removeComment(UUID postId, UUID commentId) {

        if (!this.commentRepository.existsById(commentId)) {
            String message = String.format("Post comment with id=%s not found", commentId);
            throw new ResourceNotFoundException(message);
        }

        this.commentRepository.deleteById(commentId);
    }
}
