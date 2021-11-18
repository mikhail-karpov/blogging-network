package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final UserService userService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<PostCommentDto> commentPost(@PathVariable UUID postId,
                                                      @Valid @RequestBody CreatePostCommentRequest request,
                                                      UriComponentsBuilder uriComponentsBuilder,
                                                      @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String content = request.getComment();
        PostComment postComment = postCommentService.addComment(postId, new PostComment(userId, content));

        URI location = uriComponentsBuilder.path("/posts/{postId}/comments/{commentId}").build(postId, postComment.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/posts/{postId}/comments")
    public PagedResult<PostCommentDto> findCommentsByPostId(@PathVariable UUID postId, Pageable pageable) {

        Page<PostCommentDto> commentPage = postCommentService.findAllByPostId(postId, pageable)
                .map(this::map);

        return new PagedResult<>(commentPage);
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    public PostCommentDto findCommentById(@PathVariable UUID postId, @PathVariable UUID commentId) {

        PostComment comment = findCommentOrElseThrow(commentId);
        return map(comment);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID postId, @PathVariable UUID commentId,
                              @AuthenticationPrincipal Jwt jwt) {

        PostComment comment = findCommentOrElseThrow(commentId);
        if (!comment.isOwnedBy(jwt.getSubject())) {
            throw new AccessDeniedException("Forbidden to delete comment");
        }

        postCommentService.removeComment(postId, commentId);
    }

    private PostCommentDto map(PostComment postComment) {
        String id = postComment.getId().toString();
        String userId = postComment.getUserId();
        String content = postComment.getContent();
        Instant createdDate = postComment.getCreatedDate();

        UserProfileDto user = this.userService.getUserById(postComment.getUserId());

        return new PostCommentDto(id, user, content, createdDate);
    }

    private PostComment findCommentOrElseThrow(UUID commentId) {

        return postCommentService.findById(commentId).orElseThrow(() -> {
            String message = String.format("Comment with id=%s not found", commentId);
            return new ResourceNotFoundException(message);
        });
    }
}
