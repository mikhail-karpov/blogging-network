package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@Sql(scripts = "/db_scripts/insert_users.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void givenSqlScript_whenGetById_thenFound() {
        //when
        Optional<UserProfileIntf> profile = this.userProfileRepository.findUserProfileById("1");

        //then
        assertThat(profile).isPresent();
    }

    @Test
    void givenSqlScript_whenFindByUsername_thenFound() {
        //given
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));

        //when
        Page<UserProfileIntf> profiles =
                this.userProfileRepository.findAllByUsernameContainingIgnoreCase("Smith", pageRequest);

        //then
        assertThat(profiles.getTotalPages()).isEqualTo(1);
        assertThat(profiles.getTotalElements()).isEqualTo(2L);
        assertThat(profiles.getNumberOfElements()).isEqualTo(2);
        assertThat(profiles.getContent().get(0).getId()).isEqualTo("1");
        assertThat(profiles.getContent().get(1).getId()).isEqualTo("2");
    }
}