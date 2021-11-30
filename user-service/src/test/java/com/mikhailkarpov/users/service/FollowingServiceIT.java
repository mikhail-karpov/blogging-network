package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@SqlGroup(value = {
        @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"}),
        @Sql(scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class FollowingServiceIT extends AbstractIT {

    @Autowired
    private FollowingService followingService;

    @Test
    void shouldAddFollowerAndRemoveFollower() {
        //when
        this.followingService.addToFollowers("1", "3");

        //then
        PageRequest pageRequest = PageRequest.of(0, 3);
        assertThat(this.followingService.findFollowers("1", pageRequest).getTotalElements()).isEqualTo(1L);
        assertThat(this.followingService.findFollowing("3", pageRequest).getTotalElements()).isEqualTo(1L);

        //and when
        this.followingService.removeFromFollowers("1", "3");

        //then
        assertThat(this.followingService.findFollowers("1", pageRequest).getTotalElements()).isEqualTo(0L);
        assertThat(this.followingService.findFollowing("3", pageRequest).getTotalElements()).isEqualTo(0L);
    }

    @Test
    void shouldThrow_whenAddDuplicateFollower() {

        assertThatThrownBy(() -> this.followingService.addToFollowers("3", "1"))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void shouldThrow_whenAddFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThatThrownBy(() -> this.followingService.addToFollowers("3", notFoundId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> this.followingService.addToFollowers(notFoundId, "3"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrow_whenRemoveNotExistingFollowing() {

        assertThatThrownBy(() -> this.followingService.removeFromFollowers("2", "3"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @SqlGroup(value = {
            @Sql(scripts = "/db_scripts/insert_users.sql"),
            @Sql(scripts = "/db_scripts/delete_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    void shouldThrow_whenRemoveFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThatThrownBy(() -> this.followingService.removeFromFollowers("3", notFoundId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> this.followingService.removeFromFollowers(notFoundId, "3"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindFollowers() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileDto> followers = this.followingService.findFollowers("3", pageRequest);

        //then
        assertThat(followers.getTotalPages()).isEqualTo(1);
        assertThat(followers.getTotalElements()).isEqualTo(2L);
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("1");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("2");
    }

    @Test
    void shouldFindFollowing() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileDto> followers = this.followingService.findFollowing("1", pageRequest);

        //then
        assertThat(followers.getTotalPages()).isEqualTo(1);
        assertThat(followers.getTotalElements()).isEqualTo(2L);
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("2");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("3");
    }
}
