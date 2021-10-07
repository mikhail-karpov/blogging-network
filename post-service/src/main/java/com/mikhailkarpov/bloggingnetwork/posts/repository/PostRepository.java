package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends PagingAndSortingRepository<Post, UUID> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.postComments c WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") UUID id);

    Page<Post> findByUserId(String userId, Pageable pageable);

    @Query("SELECT p.postComments FROM Post p WHERE p.id = :id")
    Page<PostComment> findCommentsByPostId(@Param("id") UUID postId, Pageable pageable);
}
