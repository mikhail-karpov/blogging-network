package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.config.JwtDecoderTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig.FOLLOW_ROUTING_KEY;
import static com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig.POST_CREATED_ROUTING_KEY;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.CREATED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = JwtDecoderTestConfig.class)
@AutoConfigureMockMvc
public class UserFeedControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PostActivityService postActivityService;

    @Test
    void givenActivities_whenGetUserFeed_thenOk() throws Exception {
        //given
        String follower = UUID.randomUUID().toString();
        String user1 = UUID.randomUUID().toString();
        String user2 = UUID.randomUUID().toString();
        String post1 = UUID.randomUUID().toString();
        String post2 = UUID.randomUUID().toString();

        this.rabbitTemplate.convertAndSend(
                FollowingEventListenerConfig.TOPIC_EXCHANGE, FOLLOW_ROUTING_KEY, new FollowingEvent(follower, user1, FOLLOWED));
        this.rabbitTemplate.convertAndSend(
                FollowingEventListenerConfig.TOPIC_EXCHANGE, FOLLOW_ROUTING_KEY, new FollowingEvent(follower, user2, FOLLOWED));

        this.rabbitTemplate.convertAndSend(
                PostEventListenerConfig.TOPIC_EXCHANGE, POST_CREATED_ROUTING_KEY, new PostEvent(post1, user1, CREATED));
        this.rabbitTemplate.convertAndSend(
                PostEventListenerConfig.TOPIC_EXCHANGE, POST_CREATED_ROUTING_KEY, new PostEvent(post2, user2, CREATED));

        Awaitility.await().atMost(60L, TimeUnit.SECONDS)
                .until(() -> this.postActivityService.getFeed(follower, PageRequest.of(0, 2)).size() == 2);

        //when
        this.mockMvc.perform(get("/feed")
                .with(jwt().jwt(jwt -> jwt.subject(follower))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.[0].id").value(post1))
                .andExpect(jsonPath("$.[1].id").value(post2));
    }
}
