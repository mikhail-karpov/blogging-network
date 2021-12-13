package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.repository.FollowingRepository;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowingServiceImpl implements FollowingService {

    private final UserProfileRepository userProfileRepository;
    private final FollowingRepository followingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void addToFollowers(String userId, String followerId) {

        if (this.followingRepository.existsById(new FollowingId(followerId, userId))) {
            throw new ResourceAlreadyExistsException("Relationship already exists");
        }

        UserProfile user = getUserById(userId);
        UserProfile follower = getUserById(followerId);

        Following following = new Following(follower, user);
        this.followingRepository.save(following);

        FollowingEvent event = new FollowingEvent(followerId, userId, FollowingEvent.Status.FOLLOWED);
        this.eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findFollowers(String userId, Pageable pageable) {

        return this.followingRepository.findFollowers(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findFollowing(String userId, Pageable pageable) {

        return this.followingRepository.findFollowing(userId, pageable);
    }

    @Override
    @Transactional
    public void removeFromFollowers(String userId, String followerId) {

        FollowingId id = new FollowingId(followerId, userId);
        if (!this.followingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relationship not found");
        }
        this.followingRepository.deleteById(id);

        FollowingEvent event = new FollowingEvent(followerId, userId, FollowingEvent.Status.UNFOLLOWED);
        this.eventPublisher.publishEvent(event);
    }

    private UserProfile getUserById(String userId) {

        return this.userProfileRepository.findById(userId).orElseThrow(() -> {
            String message = String.format("User with id=%s not found", userId);
            return new ResourceNotFoundException(message);
        });
    }
}
