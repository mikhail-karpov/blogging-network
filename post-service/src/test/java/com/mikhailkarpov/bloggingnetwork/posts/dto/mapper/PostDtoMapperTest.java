package com.mikhailkarpov.bloggingnetwork.posts.dto.mapper;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostDtoMapperTest {

    private final PostDtoMapper postDtoMapper = new PostDtoMapper();

    @Test
    void testMap() {
        //given
        Post post = new Post("user", "Post content");

        //when
        PostDto dto = postDtoMapper.map(post);

        //then
        assertThat(dto.getId()).isEqualTo(post.getId().toString());
        assertThat(dto.getUserId()).isEqualTo("user");
        assertThat(dto.getContent()).isEqualTo("Post content");
        assertThat(dto.getCreatedDate()).isEqualTo(post.getCreatedDate());
    }

}