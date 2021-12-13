package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {PersistenceTestConfig.class})
@Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
class FollowingRepositoryTest {

    @Autowired
    private FollowingRepository followingRepository;

    @Test
    void givenFollowers_whenFindFollowers_thenFound() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<UserProfileDto> followers = this.followingRepository.findFollowers("3", pageRequest);

        //then
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("1");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("2");
    }

    @Test
    void givenFollowers_whenFindFollowing_thenFound() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<UserProfileDto> following = this.followingRepository.findFollowing("1", pageRequest);

        //then
        assertThat(following.getNumberOfElements()).isEqualTo(2);
        assertThat(following.getContent().get(0).getId()).isEqualTo("2");
        assertThat(following.getContent().get(1).getId()).isEqualTo("3");
    }
}