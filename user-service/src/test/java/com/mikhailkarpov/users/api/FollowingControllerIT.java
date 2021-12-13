package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.config.RabbitListenerTestConfig;
import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.domain.Following;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.messaging.FollowingEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ContextConfiguration(classes = {SecurityTestConfig.class, RabbitListenerTestConfig.class})
@SqlGroup(value = {
        @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"}),
        @Sql(
                scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
                config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
        )
})
public class FollowingControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<PagedResult<UserProfileDto>> pagedResultTester;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Captor
    private ArgumentCaptor<FollowingEvent> eventArgumentCaptor;

    private final UserProfileDto johnSmith = new UserProfileDto("1", "johnsmith");

    private final UserProfileDto adamSmith = new UserProfileDto("2", "adamsmith");

    private final UserProfileDto jamesBond = new UserProfileDto("3", "jamesbond");

    @Test
    void shouldFollow_andUnfollow() throws Exception {

        RabbitListenerTestConfig.TestListener listener =
                this.harness.getSpy(RabbitListenerTestConfig.TestListener.LISTENER_ID);
        assertNotNull(listener);

        LatchCountDownAndCallRealMethodAnswer answer =
                this.harness.getLatchAnswerFor(RabbitListenerTestConfig.TestListener.LISTENER_ID, 2);
        doAnswer(answer).when(listener).handle(any());

        this.mockMvc.perform(post("/users/1/followers")
                .with(jwt().jwt(jwt -> jwt.subject("3"))))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1/followers")
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.result[0].userId").value("3"));

        this.mockMvc.perform(delete("/users/1/followers")
                .with(jwt().jwt(jwt -> jwt.subject("3"))))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1/followers")
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(0));

        assertTrue(answer.await(30));
        verify(listener, times(2)).handle(this.eventArgumentCaptor.capture());
        verifyEvents(this.eventArgumentCaptor.getAllValues());
    }

    private void verifyEvents(List<FollowingEvent> capturedEvents) {
        for (FollowingEvent event : capturedEvents) {
            assertEquals("3", event.getFollowerId());
            assertEquals("1", event.getFollowingId());
        }

        assertEquals(2, capturedEvents.size());
        assertEquals(FollowingEvent.Status.FOLLOWED, capturedEvents.get(0).getStatus());
        assertEquals(FollowingEvent.Status.UNFOLLOWED, capturedEvents.get(1).getStatus());
    }

    @Test
    void shouldGetFollowers() throws Exception {
        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/users/3/followers?page=0&size=3")
                .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedResultTester.write(
                new PagedResult<>(Arrays.asList(johnSmith, adamSmith), 0, 1, 2L)
        ).getJson());
    }

    @Test
    void shouldGetFollowing() throws Exception {
        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/users/1/following?page=0&size=3")
                .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(pagedResultTester.write(
                new PagedResult<>(Arrays.asList(adamSmith, jamesBond), 0, 1, 2L)
        ).getJson());
    }
}
