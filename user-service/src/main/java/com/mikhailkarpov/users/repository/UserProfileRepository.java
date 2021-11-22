package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, String> {

    boolean existsByUsernameOrEmail(String username, String email);

    Page<UserProfile> findAllByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
