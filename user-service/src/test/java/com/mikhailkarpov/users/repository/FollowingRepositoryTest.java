package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
class FollowingRepositoryTest {

    @Autowired
    private FollowingRepository followingRepository;

    private final PageRequest pageRequest =
            PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));

    @Test
    void givenSqlScripts_whenGetFollowers_thenFound() {
        //when
        Page<UserProfileIntf> followers = this.followingRepository.findFollowers("3", pageRequest);

        //then
        assertThat(followers.getTotalPages()).isEqualTo(1);
        assertThat(followers.getTotalElements()).isEqualTo(2L);
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("1");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("2");
    }

    @Test
    void givenSqlScripts_whenGetFollowing_thenFound() {
        //when
        Page<UserProfileIntf> followers = this.followingRepository.findFollowing("1", pageRequest);

        //then
        assertThat(followers.getTotalPages()).isEqualTo(1);
        assertThat(followers.getTotalElements()).isEqualTo(2L);
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("2");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("3");
    }
}