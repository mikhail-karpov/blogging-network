package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.repository.FollowingRepository;
import com.mikhailkarpov.users.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.UUID;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PersistenceTestConfig.class)
@SqlGroup(value = {
        @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"}),
        @Sql(
                scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class FollowingServiceImplTest {

    @Autowired
    private FollowingRepository followingRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<FollowingEvent> eventArgumentCaptor;

    private FollowingServiceImpl followingService;

    @BeforeEach
    void setUp() {
        this.followingService
                = new FollowingServiceImpl(this.userProfileRepository, this.followingRepository, this.eventPublisher);
    }

    @Test
    void whenAddFollower_thenAddedAndEventIsPublished() throws InterruptedException {
        this.followingService.addToFollowers("1", "3");

        PageRequest pageRequest = PageRequest.of(0, 3);
        assertEquals(1L, this.followingService.findFollowers("1", pageRequest).getTotalElements());
        assertEquals(1L, this.followingService.findFollowing("3", pageRequest).getTotalElements());

        verify(this.eventPublisher).publishEvent(this.eventArgumentCaptor.capture());
        assertEquals("3", this.eventArgumentCaptor.getValue().getFollowerId());
        assertEquals("1", this.eventArgumentCaptor.getValue().getFollowingId());
        assertEquals(FOLLOWED, this.eventArgumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrow_whenAddDuplicateFollower() {

        assertThrows(ResourceAlreadyExistsException.class,
                () -> this.followingService.addToFollowers("3", "1"));

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void shouldThrow_whenAddFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThrows(ResourceNotFoundException.class,
                () -> this.followingService.addToFollowers("3", notFoundId));

        assertThrows(ResourceNotFoundException.class,
                () -> this.followingService.addToFollowers(notFoundId, "3"));

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void whenRemoveFollower_thenRemovedAndEventIsPublished() throws InterruptedException {
        this.followingService.removeFromFollowers("3", "1");

        verify(this.eventPublisher).publishEvent(this.eventArgumentCaptor.capture());
        assertEquals("1", this.eventArgumentCaptor.getValue().getFollowerId());
        assertEquals("3", this.eventArgumentCaptor.getValue().getFollowingId());
        assertEquals(UNFOLLOWED, this.eventArgumentCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrow_whenRemoveNotExistingFollowing() {

        assertThrows(ResourceNotFoundException.class,
                () -> this.followingService.removeFromFollowers("2", "3"));

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void shouldThrow_whenRemoveFollowerAndUserNotFound() {
        //given
        String notFoundId = UUID.randomUUID().toString();

        //then
        assertThrows(ResourceNotFoundException.class,
                () -> this.followingService.removeFromFollowers("3", notFoundId));

        assertThrows(ResourceNotFoundException.class,
                () -> this.followingService.removeFromFollowers(notFoundId, "3"));

        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void shouldFindFollowers() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileDto> followers = this.followingService.findFollowers("3", pageRequest);

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
        Page<UserProfileDto> following = this.followingService.findFollowing("1", pageRequest);

        //then
        assertEquals(1, following.getTotalPages());
        assertEquals(2L, following.getTotalElements());
        assertEquals(2, following.getNumberOfElements());
        assertEquals("2", following.getContent().get(0).getId());
        assertEquals("3", following.getContent().get(1).getId());
    }
}
