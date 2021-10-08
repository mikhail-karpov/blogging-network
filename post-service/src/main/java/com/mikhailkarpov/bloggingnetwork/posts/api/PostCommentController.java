package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.DtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostCommentService;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final DtoMapper<PostComment, PostCommentDto> commentDtoMapper;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<PostCommentDto> commentPost(@PathVariable UUID postId,
                                                      @Valid @RequestBody CreatePostCommentRequest request,
                                                      UriComponentsBuilder uriComponentsBuilder,
                                                      @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String content = request.getComment();
        PostComment postComment = postCommentService.addComment(postId, new PostComment(userId, content));
        PostCommentDto dto = commentDtoMapper.map(postComment);

        URI location = uriComponentsBuilder.path("/posts/{postId}/comments/{commentId}").build(postId, dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/posts/{postId}/comments")
    public PagedResult<PostCommentDto> findCommentsByPostId(@PathVariable UUID postId, Pageable pageable) {

        Page<PostComment> commentPage = postCommentService.findAllByPostId(postId, pageable);
        Page<PostCommentDto> dtoPage = commentPage.map(commentDtoMapper::map);

        return new PagedResult<>(dtoPage);
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    public PostCommentDto findCommentById(@PathVariable UUID postId, @PathVariable UUID commentId) {

        PostComment comment = findCommentOrElseThrow(commentId);
        return commentDtoMapper.map(comment);
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

    private PostComment findCommentOrElseThrow(UUID commentId) {

        return postCommentService.findById(commentId).orElseThrow(() -> {
            String message = String.format("Comment with id=%s not found", commentId);
            return new ResourceNotFoundException(message);
        });
    }
}
