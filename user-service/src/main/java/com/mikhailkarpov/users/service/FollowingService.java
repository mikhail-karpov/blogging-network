package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.UserProfileIntf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowingService {

    void addToFollowers(String userId, String followerId);

    Page<UserProfileIntf> findFollowers(String userId, Pageable pageable);

    Page<UserProfileIntf> findFollowing(String userId, Pageable pageable);

    void removeFromFollowers(String userId, String followerId);

}
