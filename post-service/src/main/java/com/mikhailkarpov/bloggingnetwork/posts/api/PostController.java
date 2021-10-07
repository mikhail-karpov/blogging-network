package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.mikhailkarpov.bloggingnetwork.posts.dto.CreatePostRequest;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PagedResult;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.DtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.PostDtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final DtoMapper<Post, PostDto> postDtoMapper;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request,
                                              UriComponentsBuilder uriComponentsBuilder) {

        String userId = UUID.randomUUID().toString(); //todo extract from jwt
        String content = request.getContent();
        Post post = postService.save(new Post(userId, content));
        PostDto dto = postDtoMapper.map(post);

        URI location = uriComponentsBuilder.path("/posts/{id}").build(dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping
    public PagedResult<PostDto> findAll(Pageable pageable) {

        Page<Post> postPage = postService.findAll(pageable);
        Page<PostDto> dtoPage = postPage.map(postDtoMapper::map);

        return new PagedResult<>(dtoPage);
    }

    @GetMapping("/{id}")
    public PostDto findById(@PathVariable("id") String postId) {

        try {
            UUID uuid = UUID.fromString(postId);
            Optional<Post> post = postService.findById(uuid, false);
            return postDtoMapper.map(post.get());

        } catch (IllegalArgumentException | NoSuchElementException e) {
            String message = String.format("Post with id='%s' not found", postId);
            throw new ResourceNotFoundException(message);
        }
    }

    @GetMapping("/users/{id}")
    public PagedResult<PostDto> findAllByUserId(@PathVariable("id") String userId, Pageable pageable) {

        Page<Post> postPage = postService.findAllByUserId(userId, pageable);
        Page<PostDto> dtoPage = postPage.map(postDtoMapper::map);

        return new PagedResult<>(dtoPage);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePostById(@PathVariable("id") String postId) {

        try {
            UUID uuid = UUID.fromString(postId);
            postService.deleteById(uuid);

        } catch (IllegalArgumentException e) {
            String message = String.format("Post with id='%s' not found", postId);
            throw new ResourceNotFoundException(message);
        }
    }
}
