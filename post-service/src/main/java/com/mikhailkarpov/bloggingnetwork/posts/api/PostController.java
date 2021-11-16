package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import com.mikhailkarpov.bloggingnetwork.posts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request,
                                              UriComponentsBuilder uriComponentsBuilder,
                                              @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String content = request.getContent();
        Post post = postService.save(new Post(userId, content));

        URI location = uriComponentsBuilder.path("/posts/{id}").build(post.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public PostDto findById(@PathVariable("id") UUID postId) {

        Post post = findPost(postId);

        return PostDto.builder()
                .id(post.getId().toString())
                .content(post.getContent())
                .createdDate(post.getCreatedDate())
                .user(this.userService.getUserById(post.getUserId()))
                .build();
    }

    @GetMapping("/users/{id}")
    public PagedResult<PostDto> findAllByUserId(@PathVariable("id") String userId, Pageable pageable) {

        UserProfileDto user = this.userService.getUserById(userId);

        Page<PostDto> posts = this.postService.findAllByUserId(userId, pageable)
                .map(post ->
                        PostDto.builder()
                                .id(post.getId().toString())
                                .content(post.getContent())
                                .createdDate(post.getCreatedDate())
                                .user(user)
                                .build()
                );

        return new PagedResult<>(posts);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePostById(@PathVariable("id") UUID postId, @AuthenticationPrincipal Jwt jwt) {

        Post post = findPost(postId);
        if (!jwt.getSubject().equals(post.getUserId())) {
            throw new AccessDeniedException("Forbidden to delete post");
        }

        postService.deleteById(postId);
    }

    private Post findPost(UUID postId) {

        return postService.findById(postId, false).orElseThrow(() -> {
            String message = String.format("Post with id='%s' not found", postId);
            return new ResourceNotFoundException(message);
        });
    }
}
