package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
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
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional
    public void deleteById(UUID postId) {

        Optional<Post> post = findById(postId, true);
        if (!post.isPresent()) {
            String message = String.format("Post with id='%s' not found", postId);
            throw new ResourceNotFoundException(message);
        }

        postRepository.deleteById(post.get().getId());
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public Page<Post> findAllByUserId(String userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable);
    }

    @Override
    public Optional<Post> findById(UUID postId) {
        return findById(postId, false);
    }

    @Override
    public Optional<Post> findById(UUID postId, boolean commentsIncluded) {

        if (commentsIncluded) {
            return postRepository.findByIdWithComments(postId);

        } else {
            return postRepository.findById(postId);
        }
    }

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }
}
