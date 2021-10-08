package com.mikhailkarpov.bloggingnetwork.posts.dto.mapper;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.springframework.stereotype.Component;

@Component
public class PostDtoMapper implements DtoMapper<Post, PostDto> {

    @Override
    public PostDto map(Post post) {
        return PostDto.builder()
                .id(post.getId().toString())
                .userId(post.getUserId())
                .content(post.getContent())
                .createdDate(post.getCreatedDate())
                .build();
    }
}
