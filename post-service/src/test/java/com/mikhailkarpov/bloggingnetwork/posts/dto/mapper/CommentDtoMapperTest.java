package com.mikhailkarpov.bloggingnetwork.posts.dto.mapper;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import com.mikhailkarpov.bloggingnetwork.posts.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoMapperTest {

    private CommentDtoMapper dtoMapper = new CommentDtoMapper();

    @Test
    void testMap() {
        //given
        PostComment postComment = new PostComment("user", "post comment");

        //when
        PostCommentDto dto = dtoMapper.map(postComment);

        //then
        Assertions.assertThat(dto.getId()).isEqualTo(postComment.getId().toString());
        Assertions.assertThat(dto.getUserId()).isEqualTo("user");
        Assertions.assertThat(dto.getComment()).isEqualTo("post comment");
        Assertions.assertThat(dto.getCreatedDate()).isEqualTo(postComment.getCreatedDate());
    }
}