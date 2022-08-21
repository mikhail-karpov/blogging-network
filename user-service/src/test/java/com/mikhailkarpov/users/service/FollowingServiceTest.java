package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.AbstractIT;
import com.mikhailkarpov.users.dto.FollowingNotification;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static com.mikhailkarpov.users.dto.FollowingNotification.Status.FOLLOWED;
import static com.mikhailkarpov.users.dto.FollowingNotification.Status.UNFOLLOWED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@SpringBootTest
@Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
@Sql(scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"}, executionPhase = AFTER_TEST_METHOD)
class FollowingServiceTest extends AbstractIT {

    @Autowired
    private FollowingService followingService;

    @MockBean
    private NotificationService<FollowingNotification> notificationService;

    @Captor
    private ArgumentCaptor<FollowingNotification> notificationCaptor;

    @Test
    void whenAddFollower_thenAddedAndEventIsPublished() {
        followingService.addToFollowers("1", "3");

        verify(notificationService).send(notificationCaptor.capture());

        assertEquals("1", notificationCaptor.getValue().getFollowingId());
        assertEquals("3", notificationCaptor.getValue().getFollowerId());
        assertEquals(FOLLOWED, notificationCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrow_whenAddDuplicateFollower() {

        assertThrows(ResourceAlreadyExistsException.class,
                () -> followingService.addToFollowers("3", "1"));

    }

    @Test
    void shouldThrow_whenAddFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThrows(ResourceNotFoundException.class,
                () -> followingService.addToFollowers("3", notFoundId));

        assertThrows(ResourceNotFoundException.class,
                () -> followingService.addToFollowers(notFoundId, "3"));

    }

    @Test
    void whenRemoveFollower_thenRemovedAndEventIsPublished() {

        followingService.removeFromFollowers("3", "1");

        verify(notificationService).send(notificationCaptor.capture());

        assertEquals("3", notificationCaptor.getValue().getFollowingId());
        assertEquals("1", notificationCaptor.getValue().getFollowerId());
        assertEquals(UNFOLLOWED, notificationCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrow_whenRemoveNotExistingFollowing() {

        assertThrows(ResourceNotFoundException.class,
                () -> followingService.removeFromFollowers("2", "3"));

    }

    @Test
    void shouldThrow_whenRemoveFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThrows(ResourceNotFoundException.class,
                () -> followingService.removeFromFollowers("3", notFoundId));

        assertThrows(ResourceNotFoundException.class,
                () -> followingService.removeFromFollowers(notFoundId, "3"));

    }

    @Test
    void shouldFindFollowers() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileDto> followers = followingService.findFollowers("3", pageRequest);

        //then
        assertEquals(1, followers.getTotalPages());
        assertEquals(2L, followers.getTotalElements());
        assertEquals(2, followers.getNumberOfElements());
        assertEquals("1", followers.getContent().get(0).getId());
        assertEquals("2", followers.getContent().get(1).getId());
    }

    @Test
    void shouldFindFollowing() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileDto> following = followingService.findFollowing("1", pageRequest);

        //then
        assertEquals(1, following.getTotalPages());
        assertEquals(2L, following.getTotalElements());
        assertEquals(2, following.getNumberOfElements());
        assertEquals("2", following.getContent().get(0).getId());
        assertEquals("3", following.getContent().get(1).getId());
    }
}
