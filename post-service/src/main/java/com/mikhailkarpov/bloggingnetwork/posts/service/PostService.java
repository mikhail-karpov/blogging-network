package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostService {

    UUID createPost(String userId, String content);

    void deleteById(UUID postId);

    Page<PostDto> findAllByUserId(String userId, Pageable pageable);

    Optional<PostDto> findById(UUID postId);
}
