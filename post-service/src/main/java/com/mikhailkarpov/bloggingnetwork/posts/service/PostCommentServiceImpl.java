package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostService postService;
    private final PostCommentRepository commentRepository;

    @Override
    @Transactional
    public PostComment addComment(UUID postId, PostComment postComment) {

        Post post = postService.findById(postId, true).orElseThrow(() -> {
            String message = String.format("Post with id='%s' not found", postId);
            return new ResourceNotFoundException(message);
        });

        post.addComment(postComment);
        postService.save(post);
        return postComment;
    }

    @Override
    public Page<PostComment> findAllByPostId(UUID postId, Pageable pageable) {

        return commentRepository.findAllByPostId(postId, pageable);
    }

    @Override
    public Optional<PostComment> findById(UUID commentId) {

        return commentRepository.findById(commentId);
    }

    @Override
    public void removeComment(UUID postId, UUID commentId) {

        commentRepository.deleteById(commentId);
    }
}
