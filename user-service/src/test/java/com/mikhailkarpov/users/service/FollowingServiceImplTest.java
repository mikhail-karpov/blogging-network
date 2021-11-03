package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.exception.ResourceNotFoundException;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import com.mikhailkarpov.users.repository.FollowingRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FollowingServiceImplTest {

    @Mock
    private FollowingRepository followingRepository;

    @Mock
    private UserService userService;

    @Mock
    private FollowingEventPublisher eventPublisher;

    @InjectMocks
    private FollowingServiceImpl followingService;

    @Captor
    private ArgumentCaptor<Following> followingArgumentCaptor;

    @Captor
    private ArgumentCaptor<FollowingEvent> followingEventArgumentCaptor;

    private final String followerId = "followerId";
    private final String userId = "userId";
    private final FollowingId followingId = new FollowingId(followerId, userId);

    @Disabled
    @Test
    void givenFollowingNotFound_whenAddToFollowers_thenSaved() {
        //given
        when(this.followingRepository.existsById(this.followingId)).thenReturn(false);

        //when
        this.followingService.addToFollowers(this.userId, this.followerId);

        //then
        verify(this.followingRepository).save(this.followingArgumentCaptor.capture());
        verify(this.eventPublisher).publish(this.followingEventArgumentCaptor.capture());

        //and
        Following following = this.followingArgumentCaptor.getValue();
        assertThat(following.getFollower().getId()).isEqualTo(this.followerId);
        assertThat(following.getUser().getId()).isEqualTo(this.userId);

        //and
        FollowingEvent event = this.followingEventArgumentCaptor.getValue();
        assertThat(event.getFollowerId()).isEqualTo(this.followerId);
        assertThat(event.getFollowingId()).isEqualTo(this.userId);
        assertThat(event.getStatus()).isEqualTo(FOLLOWED);
    }

    @Test
    void givenFollowingFound_whenAddToFollowers_thenThrown() {
        //given
        when(this.followingRepository.existsById(this.followingId)).thenReturn(true);

        //when
        assertThatThrownBy(() -> this.followingService.addToFollowers(this.userId, this.followerId))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        //then
        verify(this.followingRepository).existsById(this.followingId);
        verifyNoMoreInteractions(this.followingRepository);
        verifyNoInteractions(this.eventPublisher);
    }

    @Test
    void findFollowers() {
    }

    @Test
    void findFollowings() {
    }

    @Test
    void givenFollowingFound_whenRemoveFromFollowers_thenRemovedAndMessageSent() {
        //given
        when(followingRepository.existsById(this.followingId)).thenReturn(true);

        //when
        this.followingService.removeFromFollowers(this.userId, this.followerId);

        //then
        verify(this.followingRepository).deleteById(this.followingId);
        verify(this.eventPublisher).publish(this.followingEventArgumentCaptor.capture());

        //and
        FollowingEvent event = this.followingEventArgumentCaptor.getValue();
        assertThat(event.getFollowerId()).isEqualTo(this.followerId);
        assertThat(event.getFollowingId()).isEqualTo(this.userId);
        assertThat(event.getStatus()).isEqualTo(UNFOLLOWED);
    }

    @Test
    void givenFollowingNotFound_whenRemoveFromFollowers_thenThrown() {
        //given
        when(followingRepository.existsById(this.followingId)).thenReturn(false);

        //when
        assertThatThrownBy(() -> this.followingService.removeFromFollowers(this.userId, this.followerId))
                .isInstanceOf(ResourceNotFoundException.class);

        //then
        verify(this.followingRepository).existsById(this.followingId);
        verifyNoMoreInteractions(this.followingRepository);
        verifyNoInteractions(this.eventPublisher);
    }
}