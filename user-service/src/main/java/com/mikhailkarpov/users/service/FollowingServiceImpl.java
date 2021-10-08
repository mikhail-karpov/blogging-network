package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.repository.FollowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowingServiceImpl implements FollowingService {

    private final FollowingRepository followingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void addToFollowers(String userId, String followerId) {

        if (followingRepository.existsById(new FollowingId(followerId, userId))) {
            String message = String.format("User with id=%s follows user with id=%s", followerId, userId);
            throw new ResourceAlreadyExistsException(message);
        }

        UserProfile user = getUserById(userId);
        UserProfile follower = getUserById(followerId);
        followingRepository.save(new Following(follower, user));
    }

    @Override
    public Page<UserProfile> findFollowers(String userId, Pageable pageable) {

        Page<UserProfile> followersPage = followingRepository.findFollowers(userId, pageable);
        return followersPage;
    }

    @Override
    public Page<UserProfile> findFollowings(String userId, Pageable pageable) {

        Page<UserProfile> followingsPage = followingRepository.findFollowings(userId, pageable);
        return followingsPage;
    }

    @Override
    @Transactional
    public void removeFromFollowers(String userId, String followerId) {

        FollowingId followingId = new FollowingId(followerId, userId);
        if (followingRepository.existsById(followingId)) {
            followingRepository.deleteById(followingId);
        } else {
            throw new ResourceNotFoundException("Following not found");
        }
    }

    private UserProfile getUserById(String userId) {

        return userService.findById(userId).orElseThrow(() -> {
            String message = String.format("User with id=%s not found", userId);
            return new ResourceNotFoundException(message);
        });
    }
}
