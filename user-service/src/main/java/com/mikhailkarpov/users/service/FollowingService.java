package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowingService {

    void addToFollowers(String userId, String followerId);

    Page<UserProfile> findFollowers(String userId, Pageable pageable);

    Page<UserProfile> findFollowings(String userId, Pageable pageable);

    void removeFromFollowers(String userId, String followerId);

}
