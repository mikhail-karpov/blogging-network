package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostCommentRepository extends PagingAndSortingRepository<PostComment, UUID> {

    Page<PostComment> findAllByPostId(UUID postId, Pageable pageable);

}
