package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostService {

    void deleteById(UUID postId);

    Page<Post> findAll(Pageable pageable);

    Page<Post> findAllByUserId(String userId, Pageable pageable);

    Optional<Post> findById(UUID postId);

    Optional<Post> findById(UUID postId, boolean commentsIncluded);

    Post save(Post post);

}
