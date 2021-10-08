package com.mikhailkarpov.bloggingnetwork.posts.dto.mapper;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostCommentDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommentDtoMapper implements DtoMapper<PostComment, PostCommentDto> {

    @Override
    public PostCommentDto map(PostComment postComment) {
        String id = postComment.getId().toString();
        String userId = postComment.getUserId();
        String content = postComment.getContent();
        LocalDateTime createdDate = postComment.getCreatedDate();

        return new PostCommentDto(id, userId, content, createdDate);
    }
}
