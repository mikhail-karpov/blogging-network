package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@Sql(
        scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
@Sql(
        scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FollowingServiceExceptionHandlingIT extends AbstractIT {

    @Autowired
    private FollowingService followingService;

    @MockBean
    private FollowingEventPublisher eventPublisher;

    @Test
    void givenFollowingExists_whenAddFollower_thenException() {
        //when
        assertThatThrownBy(() -> this.followingService.addToFollowers("3", "1"))
                .isExactlyInstanceOf(ResourceAlreadyExistsException.class);

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void givenNotFoundUser_whenAddFollower_thenException() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThatThrownBy(() -> this.followingService.addToFollowers("1", notFoundId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> this.followingService.addToFollowers(notFoundId, "1"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void givenNotFoundUser_whenRemoveFollower_thenException() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThatThrownBy(() -> this.followingService.removeFromFollowers("1", notFoundId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> this.followingService.removeFromFollowers(notFoundId, "1"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void givenFollowingDoesNotExist_whenRemoveFollower_thenException() {
        //then
        assertThatThrownBy(() -> this.followingService.removeFromFollowers("1", "3"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void givenException_whenAddFollower_thenRollback() {
        //given
        doThrow(RuntimeException.class).when(this.eventPublisher).publish(any());

        //when
        assertThatThrownBy(() -> this.followingService.addToFollowers("1", "3"))
                .isInstanceOf(RuntimeException.class);

        //then
        assertThat(this.followingService.findFollowers("1", PageRequest.of(0, 2)).getTotalElements())
                .isEqualTo(0L);
    }

    @Test
    void givenException_whenRemoveFollower_thenRollback() {
        //given
        doThrow(RuntimeException.class).when(this.eventPublisher).publish(any());

        //when
        assertThatThrownBy(() -> this.followingService.removeFromFollowers("3", "1"))
                .isInstanceOf(RuntimeException.class);

        //then
        assertThat(this.followingService.findFollowers("3", PageRequest.of(0, 2)).getTotalElements())
                .isEqualTo(2L);
    }
}
