package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
class UserProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void existsByUsernameOrEmail() {
        //given
        String userId = UUID.randomUUID().toString();
        String email = "fake@example.com";
        String username = "username";
        UserProfile userProfile = new UserProfile(userId, username, email);

        //when
        entityManager.persist(userProfile);

        //then
        assertTrue(userProfileRepository.existsById(userId));
        assertTrue(userProfileRepository.existsByUsernameOrEmail(username, email));
        assertTrue(userProfileRepository.existsByUsernameOrEmail(username, "a" + email));
        assertFalse(userProfileRepository.existsByUsernameOrEmail("a" + username, "b" + email));
    }
}