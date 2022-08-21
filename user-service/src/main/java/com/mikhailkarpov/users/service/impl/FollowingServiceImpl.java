package com.mikhailkarpov.users.service.impl;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.FollowingNotification;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.repository.FollowingRepository;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import com.mikhailkarpov.users.service.FollowingService;
import com.mikhailkarpov.users.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mikhailkarpov.users.dto.FollowingNotification.Status.FOLLOWED;
import static com.mikhailkarpov.users.dto.FollowingNotification.Status.UNFOLLOWED;

@Service
@RequiredArgsConstructor
public class FollowingServiceImpl implements FollowingService {

    private final UserProfileRepository userProfileRepository;
    private final FollowingRepository followingRepository;
    private final NotificationService<FollowingNotification> followingNotificationService;

    @Override
    @Transactional
    public void addToFollowers(String userId, String followerId) {

        if (followingRepository.existsById(new FollowingId(followerId, userId))) {
            throw new ResourceAlreadyExistsException("Relationship already exists");
        }

        UserProfile user = getUserById(userId);
        UserProfile follower = getUserById(followerId);

        followingRepository.save(new Following(follower, user));
        followingNotificationService.send(new FollowingNotification(followerId, userId, FOLLOWED));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findFollowers(String userId, Pageable pageable) {

        return followingRepository.findFollowers(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDto> findFollowing(String userId, Pageable pageable) {

        return followingRepository.findFollowing(userId, pageable);
    }

    @Override
    @Transactional
    public void removeFromFollowers(String userId, String followerId) {

        FollowingId id = new FollowingId(followerId, userId);
        if (!followingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relationship not found");
        }

        followingRepository.deleteById(id);
        followingNotificationService.send(new FollowingNotification(followerId, userId, UNFOLLOWED));
    }

    private UserProfile getUserById(String userId) {

        return userProfileRepository.findById(userId).orElseThrow(() -> {
            String message = String.format("User with id=%s not found", userId);
            return new ResourceNotFoundException(message);
        });
    }
}
