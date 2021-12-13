package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostProjection;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.EventStatus;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UUID createPost(String userId, String content) {
        Post post = this.postRepository.save(new Post(userId, content));

        PostEvent event = new PostEvent(post.getId().toString(), userId, EventStatus.CREATED);
        this.eventPublisher.publishEvent(event);

        return post.getId();
    }

    @Override
    @Transactional
    public void deleteById(UUID postId) {

        PostProjection post = this.postRepository.findPostById(postId).orElseThrow(() -> {
            String message = String.format("Post with id=%s not found", postId);
            return new ResourceNotFoundException(message);
        });

        this.postRepository.deleteById(postId);

        PostEvent event = new PostEvent(postId.toString(), post.getUserId(), EventStatus.DELETED);
        this.eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> findAllByUserId(String userId, Pageable pageable) {

        UserProfileDto user = this.userService.getUserById(userId);

        return this.postRepository.findPostsByUserId(userId, pageable)
                .map(post -> PostDto.builder()
                        .id(post.getId().toString())
                        .content(post.getContent())
                        .user(user)
                        .createdDate(post.getCreatedDate())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> findById(UUID postId) {

        return this.postRepository.findPostById(postId).map(post -> PostDto.builder()
                .id(post.getId().toString())
                .content(post.getContent())
                .createdDate(post.getCreatedDate())
                .user(this.userService.getUserById(post.getUserId())).build()
        );
    }
}
