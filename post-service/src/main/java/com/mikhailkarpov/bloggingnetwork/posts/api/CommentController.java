package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.dto.CommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreateCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void commentPost(@PathVariable UUID postId,
                            @Valid @RequestBody CreateCommentRequest request,
                            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        this.commentService.createComment(postId, userId, request.getComment());
    }

    @GetMapping("/posts/{postId}/comments")
    public PagedResult<CommentDto> findCommentsByPostId(@PathVariable UUID postId, Pageable pageable) {

        Page<CommentDto> commentPage = this.commentService.findAllByPostId(postId, pageable);
        return new PagedResult<>(commentPage);
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    public CommentDto findCommentById(@PathVariable UUID postId, @PathVariable UUID commentId) {

        return this.commentService.findById(commentId);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID postId, @PathVariable UUID commentId,
                              @AuthenticationPrincipal Jwt jwt) {

        UserProfileDto user = this.commentService.findById(commentId).getUser();

        if (user == null || !user.getUserId().equals(jwt.getSubject())) {
            throw new AccessDeniedException("Forbidden to delete comment");
        }

        commentService.deleteComment(commentId);
    }
}
