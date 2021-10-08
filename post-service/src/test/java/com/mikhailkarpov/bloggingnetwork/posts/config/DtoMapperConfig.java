package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.CommentDtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.DtoMapper;
import com.mikhailkarpov.bloggingnetwork.posts.dto.mapper.PostDtoMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DtoMapperConfig {

    @Bean
    public DtoMapper<Post, PostDto> postDtoMapper() {
        return new PostDtoMapper();
    }

    @Bean
    public DtoMapper<PostComment, PostCommentDto> commentDtoMapper() {
        return new CommentDtoMapper();
    }
}
