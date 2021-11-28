package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.dto.UserProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowingService {

    void addToFollowers(String userId, String followerId);

    Page<UserProfileDto> findFollowers(String userId, Pageable pageable);

    Page<UserProfileDto> findFollowing(String userId, Pageable pageable);

    void removeFromFollowers(String userId, String followerId);

}
