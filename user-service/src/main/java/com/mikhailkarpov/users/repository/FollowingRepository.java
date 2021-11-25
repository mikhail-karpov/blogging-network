package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FollowingRepository extends CrudRepository<Following, FollowingId> {

    @Query(
            value = "SELECT new com.mikhailkarpov.users.dto.UserProfileDto" +
                    "(f.followerUser.id, f.followerUser.username) " +
                    "FROM Following f " +
                    "WHERE f.followingUser.id = :userId",
            countQuery = "SELECT count(*) FROM Following f WHERE f.followingUser.id = :userId"
    )
    Page<UserProfileIntf> findFollowers(@Param("userId") String userId, Pageable pageable);

    @Query(
            value = "SELECT new com.mikhailkarpov.users.dto.UserProfileDto" +
                    "(f.followingUser.id, f.followingUser.username) " +
                    "FROM Following f WHERE f.followerUser.id = :userId",
            countQuery = "SELECT count(*) FROM Following f WHERE f.followerUser.id = :userId"
    )
    Page<UserProfileIntf> findFollowing(@Param("userId") String userId, Pageable pageable);
}
