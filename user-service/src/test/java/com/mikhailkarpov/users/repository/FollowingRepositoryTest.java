package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FollowingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FollowingRepository followingRepository;

    private UserProfile user1 = new UserProfile("user1", "user1", "user1@example.com");
    private UserProfile user2 = new UserProfile("user2", "user2", "user2@example.com");
    private UserProfile user3 = new UserProfile("user3", "user3", "user3@example.com");

    @BeforeEach
    void saveProfiles() {
        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);
        user3 = entityManager.persist(user3);
    }

    @Test
    void testSave() {
        //when
        Following savedFollowing = followingRepository.save(new Following(user1, user2));
        Following foundFollowing = entityManager.find(Following.class, savedFollowing.getFollowingId());

        //then
        assertEquals(savedFollowing, foundFollowing);
    }

    @Test
    void testExistsById() {
        //given
        followingRepository.save(new Following(user1, user2));

        //when
        FollowingId followingId = new FollowingId(user1.getId(), user2.getId());
        boolean existsById = followingRepository.existsById(followingId);

        //then
        assertTrue(existsById);
    }

    @Test
    void testFindFollowers() {
        //given
        followingRepository.save(new Following(user1, user3));
        followingRepository.save(new Following(user2, user3));

        //when
        Page<UserProfile> followersPage = followingRepository.findFollowers(user3.getId(), PageRequest.of(0, 3));

        //then
        assertEquals(2L, followersPage.getTotalElements());
        assertIterableEquals(Arrays.asList(user1, user2), followersPage.getContent());
    }

    @Test
    void testFindFollowings() {
        //given
        followingRepository.save(new Following(user1, user2));
        followingRepository.save(new Following(user1, user3));

        //when
        Page<UserProfile> followingsPage = followingRepository.findFollowings(user1.getId(), PageRequest.of(0, 3));

        //then
        assertEquals(2L, followingsPage.getTotalElements());
        assertIterableEquals(Arrays.asList(user2, user3), followingsPage.getContent());
    }
}