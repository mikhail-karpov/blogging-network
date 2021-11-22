package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.config.JwtDecoderTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserFeed;
import com.mikhailkarpov.bloggingnetwork.feed.dto.UserProfile;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ContextConfiguration(classes = JwtDecoderTestConfig.class)
@WebMvcTest(controllers = UserFeedController.class)
@AutoConfigureJsonTesters
class UserFeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFeedService userFeedService;

    @Autowired
    private JacksonTester<UserFeed> feedTester;

    @Test
    void givenJwt_whenGetUserFeed_thenOk() throws Exception {
        //given
        String userId = "user-id";
        UserFeed userFeed = new UserFeed(Arrays.asList(
                Post.builder()
                        .id("post1")
                        .content("Post 1 content")
                        .user(new UserProfile("user1", "username1"))
                        .createdDate(LocalDateTime.now().minus(1L, ChronoUnit.DAYS))
                        .build(),
                Post.builder()
                        .id("post2")
                        .content("Post 2 content")
                        .user(new UserProfile("user2", "username2"))
                        .createdDate(LocalDateTime.now().minus(2L, ChronoUnit.DAYS))
                        .build()
        ));
        when(this.userFeedService.getUserFeed(userId, 1)).thenReturn(userFeed);

        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/feed?page=1")
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(feedTester.write(userFeed).getJson());
    }

    @Test
    void givenNoJwt_whenGetFeed_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(get("/feed?page=1"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //then
        verifyNoInteractions(this.userFeedService);
    }
}