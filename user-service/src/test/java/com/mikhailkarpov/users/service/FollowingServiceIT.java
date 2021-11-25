package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.domain.UserProfileIntf;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
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

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Sql(
        scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
@Sql(
        scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FollowingServiceIT extends AbstractIT {

    @Autowired
    private FollowingService followingService;

    @MockBean
    private FollowingEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<FollowingEvent> eventCaptor;

    @Test
    void shouldFindFollowers() {
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserProfileIntf> followers = this.followingService.findFollowers("3", pageRequest);

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
        Page<UserProfileIntf> followers = this.followingService.findFollowing("1", pageRequest);

        //then
        assertThat(followers.getTotalPages()).isEqualTo(1);
        assertThat(followers.getTotalElements()).isEqualTo(2L);
        assertThat(followers.getNumberOfElements()).isEqualTo(2);
        assertThat(followers.getContent().get(0).getId()).isEqualTo("2");
        assertThat(followers.getContent().get(1).getId()).isEqualTo("3");
    }

    @Test
    void shouldAddFollower_andPublishEvent() {
        //given
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));

        //when
        this.followingService.addToFollowers("1", "3");

        //then
        assertThat(this.followingService.findFollowers("1", pageRequest).getTotalElements()).isEqualTo(1L);
        assertThat(this.followingService.findFollowing("3", pageRequest).getTotalElements()).isEqualTo(1L);

        verify(this.eventPublisher).publish(this.eventCaptor.capture());
        assertThat(this.eventCaptor.getValue().getFollowerId()).isEqualTo("3");
        assertThat(this.eventCaptor.getValue().getFollowingId()).isEqualTo("1");
        assertThat(this.eventCaptor.getValue().getStatus()).isEqualTo(FOLLOWED);
    }

    @Test
    void shouldRemoveFollower_adPublishEvent() {
        //given
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "id"));

        //when
        this.followingService.removeFromFollowers("3", "1");

        //then
        assertThat(this.followingService.findFollowers("3", pageRequest).getTotalElements()).isEqualTo(1L);
        assertThat(this.followingService.findFollowing("1", pageRequest).getTotalElements()).isEqualTo(1L);

        verify(this.eventPublisher).publish(this.eventCaptor.capture());
        assertThat(this.eventCaptor.getValue().getFollowerId()).isEqualTo("1");
        assertThat(this.eventCaptor.getValue().getFollowingId()).isEqualTo("3");
        assertThat(this.eventCaptor.getValue().getStatus()).isEqualTo(UNFOLLOWED);
    }
}
