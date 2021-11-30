package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, String> {

    Optional<UserProfileDto> findUserProfileById(String userId);

    Page<UserProfileDto> findAllByUsernameContainingIgnoreCase(String username, Pageable pageable);

}
