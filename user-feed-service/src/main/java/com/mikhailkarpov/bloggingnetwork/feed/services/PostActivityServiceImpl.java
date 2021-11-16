package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityEntity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.POST_ACTIVITY;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostActivityServiceImpl implements PostActivityService {

    private final ActivityRepository activityRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void save(PostActivity activity) {
        ActivityEntity entity = new ActivityEntity(activity.getAuthorId(), activity.getPostId(), POST_ACTIVITY);
        this.activityRepository.save(entity);
        log.info("Saving {}", entity);
    }

    @Override
    @Transactional
    public void delete(PostActivity activity) {
        ActivityId id = new ActivityId(activity.getAuthorId(), activity.getPostId(), POST_ACTIVITY);
        this.activityRepository.deleteById(id);
        log.info("Deleting by id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostActivity> getFeed(String userId, Pageable pageable) {

        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

        CriteriaQuery<ActivityEntity> cq = cb.createQuery(ActivityEntity.class);
        Root<ActivityEntity> root = cq.from(ActivityEntity.class);

        Subquery<String> sub = cq.subquery(String.class);
        Root<ActivityEntity> subRoot = sub.from(ActivityEntity.class);

        sub.select(subRoot.get("id").get("sourceId"))
                .where(
                        cb.equal(subRoot.get("id").get("userId"), userId),
                        cb.equal(subRoot.get("id").get("type"), FOLLOWING_ACTIVITY));

        cq.select(root)
                .where(
                        cb.in(root.get("id").get("userId")).value(sub),
                        cb.equal(root.get("id").get("type"), POST_ACTIVITY),
                        cb.greaterThan(root.get("createdDate"), Instant.now().minus(7L, DAYS)))
                .orderBy(cb.asc(root.get("createdDate")));

        int firstResult = pageable != null ? pageable.getPageNumber() * pageable.getPageSize() : 0;
        int maxResults = pageable != null ? pageable.getPageSize() : 20;

        return this.entityManager.createQuery(cq)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultStream()
                .map(entity -> new PostActivity(entity.getSourceId(), entity.getUserId()))
                .collect(Collectors.toList());
    }
}
