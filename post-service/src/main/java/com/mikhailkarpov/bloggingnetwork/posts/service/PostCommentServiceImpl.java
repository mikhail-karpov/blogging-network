package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.domain.Post;
import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostService postService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PostComment addComment(UUID postId, PostComment postComment) {

        Post post = postService.findById(postId, true).orElseThrow(() -> {
            String message = String.format("Post with id='%s' not found", postId);
            return new ResourceNotFoundException(message);
        });

        post.addComment(postComment);
        postService.save(post);
        return postComment;
    }

    @Override
    @Transactional
    public Page<PostComment> findAllByPostId(UUID postId, Pageable pageable) {

        String count = "SELECT COUNT(*) FROM PostComment c WHERE c.post.id = :id";
        Long totalResults = entityManager.createQuery(count, Long.class)
                .setParameter("id", postId)
                .getSingleResult();

        List<PostComment> postComments = Collections.emptyList();

        if (totalResults != 0L) {
            String select = "SELECT c FROM PostComment c WHERE c.post.id = :id";
            postComments = entityManager.createQuery(select, PostComment.class)
                    .setParameter("id", postId)
                    .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(postComments, pageable, totalResults);
    }

    @Override
    public Optional<PostComment> findById(UUID commentId) {

        PostComment postComment = null;

        try {
            String sql = "SELECT c FROM PostComment c WHERE c.id = :id";
            postComment = entityManager.createQuery(sql, PostComment.class)
                    .setParameter("id", commentId)
                    .getSingleResult();
        } catch (Exception e) {
            //do nothing
        }

        return Optional.ofNullable(postComment);
    }

    @Override
    @Transactional
    public void removeComment(UUID postId, UUID commentId) {

        String sql = "DELETE FROM PostComment c WHERE c.id = :cId AND c.post.id =: pId";
        int rowsUpdated = entityManager.createQuery(sql)
                .setParameter("cId", commentId)
                .setParameter("pId", postId)
                .executeUpdate();

        if (rowsUpdated == 0) {
            String message = String.format("Comment with id ='%s' not found for post with id='%s'", commentId, postId);
            throw new ResourceNotFoundException(message);
        }
    }
}
