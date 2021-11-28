package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FollowingRepository extends CrudRepository<Following, FollowingId> {

    @Query(
            value = "SELECT new com.mikhailkarpov.users.dto.UserProfileDto(f.follower.id, f.follower.username) " +
                    "FROM Following f WHERE f.following.id = :id",
            countQuery = "SELECT COUNT(*) FROM Following f WHERE f.following.id = :id"
    )
    Page<UserProfileDto> findFollowers(@Param("id") String userId, Pageable pageable);

    @Query(
            value = "SELECT new com.mikhailkarpov.users.dto.UserProfileDto(f.following.id, f.following.username) " +
                    "FROM Following f WHERE f.follower.id = :id",
            countQuery = "SELECT COUNT(*) FROM Following f WHERE f.follower.id = :id"
    )
    Page<UserProfileDto> findFollowing(@Param("id") String userId, Pageable pageable);
}
