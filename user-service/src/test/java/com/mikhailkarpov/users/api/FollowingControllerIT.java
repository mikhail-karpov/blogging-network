package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.config.SecurityTestConfig;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ContextConfiguration(classes = SecurityTestConfig.class)
@SqlGroup(value = {
        @Sql(scripts = {"/db_scripts/insert_users.sql", "/db_scripts/insert_followings.sql"}),
        @Sql(scripts = {"/db_scripts/delete_followings.sql", "/db_scripts/delete_users.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class FollowingControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<PagedResult<UserProfileDto>> pagedResultTester;

    private final UserProfileDto johnSmith = new UserProfileDto("1", "johnsmith");

    private final UserProfileDto adamSmith = new UserProfileDto("2", "adamsmith");

    private final UserProfileDto jamesBond = new UserProfileDto("3", "jamesbond");

    @Test
    void shouldFollow_andUnfollow() throws Exception {
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
