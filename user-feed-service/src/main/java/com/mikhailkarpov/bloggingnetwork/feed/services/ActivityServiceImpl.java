package com.mikhailkarpov.bloggingnetwork.feed.services;

import com.mikhailkarpov.bloggingnetwork.feed.domain.Activity;
import com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityId;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.FOLLOWING_ACTIVITY;
import static com.mikhailkarpov.bloggingnetwork.feed.domain.ActivityType.POST_ACTIVITY;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void save(Activity activity) {
        this.activityRepository.save(activity);
    }

    @Override
    @Transactional
    public void deleteById(ActivityId id) {
        this.activityRepository.deleteById(id);
    }

    @Override
    public Optional<Activity> findById(ActivityId id) {
        return this.activityRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostActivity> getFeed(String userId, int page) {

        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

        CriteriaQuery<PostActivity> cq = cb.createQuery(PostActivity.class);
        Root<PostActivity> root = cq.from(PostActivity.class);

        Subquery<String> sub = cq.subquery(String.class);
        Root<Activity> subRoot = sub.from(Activity.class);

        sub.select(subRoot.get("sourceId"))
                .where(
                        cb.equal(subRoot.get("userId"), userId),
                        cb.equal(subRoot.get("activityType"), FOLLOWING_ACTIVITY));

        cq.select(root)
                .where(
                        cb.in(root.get("userId")).value(sub),
                        cb.equal(root.get("activityType"), POST_ACTIVITY),
                        cb.greaterThan(root.get("createdDate"), Instant.now().minus(7L, DAYS)))
                .orderBy(cb.asc(root.get("createdDate")));

        int maxResults = 20;
        int firstResult = page >= 0 ? maxResults * page : 0;

        return this.entityManager.createQuery(cq)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }
}
