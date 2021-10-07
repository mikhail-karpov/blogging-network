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
    public ResponseEntity<PostCommentDto> commentPost(@PathVariable String postId,
                                                      @Valid @RequestBody CreatePostCommentRequest request,
                                                      UriComponentsBuilder uriComponentsBuilder) {

        String userId = UUID.randomUUID().toString(); //todo extract from jwt
        String content = request.getComment();
        PostComment postComment = postCommentService.addComment(parseId(postId), new PostComment(userId, content));
        PostCommentDto dto = commentDtoMapper.map(postComment);

        URI location = uriComponentsBuilder.path("/posts/{postId}/comments/{commentId}").build(postId, dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/posts/{postId}/comments")
    public PagedResult<PostCommentDto> findCommentsByPostId(@PathVariable String postId, Pageable pageable) {

        Page<PostComment> commentPage = postCommentService.findAllByPostId(parseId(postId), pageable);
        Page<PostCommentDto> dtoPage = commentPage.map(commentDtoMapper::map);

        return new PagedResult<>(dtoPage);
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    public PostCommentDto findCommentById(@PathVariable String postId, @PathVariable String commentId) {

        Optional<PostComment> comment = postCommentService.findById(parseId(commentId));
        if (!comment.isPresent()) {
            String message = String.format("Comment with id=%s not found", commentId);
            throw new ResourceNotFoundException(message);
        }

        return commentDtoMapper.map(comment.get());
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable String postId, @PathVariable String commentId) {

        postCommentService.removeComment(parseId(postId), parseId(commentId));
    }

    private UUID parseId(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            String message = String.format("Resource with id='%s' not found", s);
            throw new ResourceNotFoundException(message);
        }
    }
}
