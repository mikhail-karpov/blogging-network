package com.mikhailkarpov.users.repository;

import com.mikhailkarpov.users.config.PersistenceTestConfig;
import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.domain.FollowingId;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import com.mikhailkarpov.users.messaging.FollowingEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;

import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.users.messaging.FollowingEvent.Status.UNFOLLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {PersistenceTestConfig.class})
@Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"})
class FollowingRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FollowingRepository followingRepository;

    @MockBean
    private FollowingEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<FollowingEvent> eventCaptor;

    @Test
    void givenUsers_whenSaveFollowing_thenSaved() {
        //given
        UserProfile followerUser = this.entityManager.find(UserProfile.class, "2");
        UserProfile followingUser = this.entityManager.find(UserProfile.class, "1");
        Following following = new Following(followerUser, followingUser);

        //when
        this.followingRepository.save(following);
        this.entityManager.flush();

        //then
        FollowingId id = new FollowingId("2", "1");
        assertThat(this.followingRepository.findById(id)).isPresent();
        verify(this.eventPublisher).publish(this.eventCaptor.capture());

        assertThat(this.eventCaptor.getValue().getFollowerId()).isEqualTo("2");
        assertThat(this.eventCaptor.getValue().getFollowingId()).isEqualTo("1");
        assertThat(this.eventCaptor.getValue().getStatus()).isEqualTo(FOLLOWED);
    }

    @Test
    void givenFollowers_whenRemoveFollowing_thenRemoved() {
        //given
        FollowingId id = new FollowingId("2", "3");

        //when
        this.followingRepository.deleteById(id);
        this.entityManager.flush();

        //then
        assertThat(this.followingRepository.findById(id)).isEmpty();
        verify(this.eventPublisher).publish(this.eventCaptor.capture());

        assertThat(this.eventCaptor.getValue().getFollowerId()).isEqualTo("2");
        assertThat(this.eventCaptor.getValue().getFollowingId()).isEqualTo("3");
        assertThat(this.eventCaptor.getValue().getStatus()).isEqualTo(UNFOLLOWED);
    }

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