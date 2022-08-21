package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.AbstractIT;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Sql(scripts = "/db_scripts/insert_users.sql")
@Sql(scripts = "/db_scripts/delete_users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerTest extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<UserProfileDto> profileTester;

    @Autowired
    private JacksonTester<PagedResult<UserProfileDto>> pagedResultTester;

    private final UserProfileDto johnSmith = new UserProfileDto("1", "johnsmith");

    private final UserProfileDto adamSmith = new UserProfileDto("2", "adamsmith");

    @Test
    void givenUsers_whenGetProfile_thenOk() throws Exception {
        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/users/1/profile")
                .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(this.profileTester.write(this.johnSmith).getJson());
    }

    @Test
    void givenUsers_whenSearchByUsername_thenOk() throws Exception {
        //when
        String url = "/users/search?username=Smith&size=3";
        MockHttpServletResponse response = this.mockMvc.perform(get(url)
                .with(jwt()))
                .andReturn()
                .getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(this.pagedResultTester.write(
                new PagedResult<>(Arrays.asList(johnSmith, adamSmith), 0, 1, 2L)).getJson());
    }
}