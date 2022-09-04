package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPost(@Valid @RequestBody CreatePostRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String content = request.getContent();
        this.postService.createPost(userId, content);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> findById(@PathVariable("id") UUID postId) {

        return this.postService.findById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}")
    public PagedResult<PostDto> findAllByUserId(@PathVariable("id") String userId, Pageable pageable) {

        Page<PostDto> posts = this.postService.findAllByUserId(userId, pageable);
        return new PagedResult<>(posts);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePostById(@PathVariable("id") UUID postId, @AuthenticationPrincipal Jwt jwt) {

        PostDto post = this.postService.findById(postId).orElseThrow(() -> {
            String message = String.format("Post with id=%s not found", postId);
            return new ResourceNotFoundException(message);
        });

        if (!post.getUser().getUserId().equals(jwt.getSubject())) {
            throw new AccessDeniedException("Forbidden to delete post");
        }

        this.postService.deleteById(postId);
    }
}
