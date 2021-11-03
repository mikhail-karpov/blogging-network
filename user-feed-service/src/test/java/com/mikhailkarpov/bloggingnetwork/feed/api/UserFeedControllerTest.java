package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.config.JwtDecoderTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
    private PostActivityService postActivityService;

    @Autowired
    private JacksonTester<List<PostActivity>> activityTester;

    @Test
    void givenJwt_whenGetUserFeed_thenOk() throws Exception {
        //given
        String userId = "user-id";
        List<PostActivity> activities = Arrays.asList(
                new PostActivity("user1", "post1"),
                new PostActivity("user2", "post2")
        );
        PageRequest pageRequest = PageRequest.of(1, 2);

        when(this.postActivityService.getFeed(userId, pageRequest))
                .thenReturn(new PageImpl<>(activities, pageRequest, 4L));

        //when
        MockHttpServletResponse response = this.mockMvc.perform(get("/feed?page=1&size=2")
                        .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(activityTester.write(activities).getJson());
    }

    @Test
    void givenNoJwt_whenGetFeed_thenUnauthorized() throws Exception {
        //when
        this.mockMvc.perform(get("/feed?page=1&size=2"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        //then
        verifyNoInteractions(this.postActivityService);
    }
}