package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import com.mikhailkarpov.users.repository.FollowingRepository;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;

@Service
@RequiredArgsConstructor
public class FollowingServiceImpl implements FollowingService {

    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;
    private final FollowingRepository followingRepository;
    private final FollowingEventPublisher followingEventPublisher;

    @Override
    @Transactional
    public void addToFollowers(String userId, String followerId) {

        if (this.followingRepository.existsById(new FollowingId(followerId, userId))) {
            String message = String.format("Already following user with id=%s", userId);
            throw new ResourceAlreadyExistsException(message);
        }

        UserProfile user = getUserById(userId);
        UserProfile follower = getUserById(followerId);
        this.followingRepository.save(new Following(follower, user));

        FollowingEvent event = new FollowingEvent(followerId, userId, FOLLOWED);
        this.followingEventPublisher.publish(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileIntf> findFollowers(String userId, Pageable pageable) {

        return this.followingRepository.findFollowers(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileIntf> findFollowing(String userId, Pageable pageable) {

        return this.followingRepository.findFollowing(userId, pageable);
    }

    @Override
    @Transactional
    public void removeFromFollowers(String userId, String followerId) {

        try {
            FollowingId followingId = new FollowingId(followerId, userId);
            this.followingRepository.deleteById(followingId);

            FollowingEvent event = new FollowingEvent(followerId, userId, UNFOLLOWED);
            this.followingEventPublisher.publish(event);

        } catch (EmptyResultDataAccessException e) {
            String message = String.format("Following not found");
            throw new ResourceNotFoundException(message);
        }
    }

    private UserProfile getUserById(String userId) {

        return this.userProfileRepository.findById(userId).orElseThrow(() -> {
            String message = String.format("User with id=%s not found", userId);
            return new ResourceNotFoundException(message);
        });
    }
}
