package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FollowingRepository extends CrudRepository<Following, FollowingId> {

    @Query(
            value = "SELECT f.follower FROM Following f WHERE f.user.id = :userId",
            countQuery = "SELECT count(*) FROM Following f WHERE f.user.id = :userId"
    )
    Page<UserProfile> findFollowers(@Param("userId") String userId, Pageable pageable);

    @Query(
            value = "SELECT f.user FROM Following f WHERE f.follower.id = :userId",
            countQuery = "SELECT count(*) FROM Following f WHERE f.follower.id = :userId"
    )
    Page<UserProfile> findFollowings(@Param("userId") String userId, Pageable pageable);
}
