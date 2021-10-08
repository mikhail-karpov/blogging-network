package com.mikhailkarpov.bloggingnetwork.posts.repository;

import com.mikhailkarpov.bloggingnetwork.posts.domain.PostComment;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepository {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void deleteById(UUID commentId) {

        String sql = "DELETE FROM PostComment c WHERE c.id = :id";
        int rowsUpdated = entityManager.createQuery(sql)
                .setParameter("id", commentId)
                .executeUpdate();

        if (rowsUpdated == 0) {
            String message = String.format("Comment with id ='%s' not found", commentId);
            throw new ResourceNotFoundException(message);
        }
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

        try {
            String sql = "SELECT c FROM PostComment c WHERE c.id = :id";
            PostComment postComment = entityManager.createQuery(sql, PostComment.class)
                    .setParameter("id", commentId)
                    .getSingleResult();

            return Optional.of(postComment);

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
