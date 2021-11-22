package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.config.JwtDecoderTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent;
import com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent;
import com.mikhailkarpov.bloggingnetwork.feed.services.ActivityService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    private ActivityService postActivityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenActivities_whenGetUserFeed_thenOk() throws Exception {
        //given
        String follower = UUID.randomUUID().toString();
        String user1 = UUID.randomUUID().toString();
        String user2 = UUID.randomUUID().toString();
        String post1 = UUID.randomUUID().toString();
        String post2 = UUID.randomUUID().toString();

        sendUserFollowsMessage(follower, user1);
        sendUserFollowsMessage(follower, user2);
        sendPostCreatedMessage(user1, post1);
        sendPostCreatedMessage(user2, post2);

        Awaitility.await().atMost(60L, TimeUnit.SECONDS)
                .until(() -> this.postActivityService.getFeed(follower, 0).size() == 2);

        //when
        this.mockMvc.perform(get("/feed?page=0")
                .with(jwt().jwt(jwt -> jwt.subject(follower))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.size()").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(post1))
                .andExpect(jsonPath("$.posts[1].id").value(post2));
    }

    private void sendUserFollowsMessage(String followerId, String followingUserId) throws JsonProcessingException {
        FollowingEvent event = new FollowingEvent();
        event.setFollowerUserId(followerId);
        event.setFollowingUserId(followingUserId);
        event.setStatus(FOLLOWED);

        Message message = MessageBuilder
                .withBody(this.objectMapper.writeValueAsBytes(event))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        this.rabbitTemplate.convertAndSend(FollowingEventListenerConfig.TOPIC_EXCHANGE, FOLLOW_ROUTING_KEY, message);
    }

    private void sendPostCreatedMessage(String authorId, String postId) throws JsonProcessingException {
        PostEvent event = new PostEvent();
        event.setAuthorId(authorId);
        event.setPostId(postId);
        event.setStatus(CREATED);

        Message message = MessageBuilder
                .withBody(this.objectMapper.writeValueAsBytes(event))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        this.rabbitTemplate.convertAndSend(PostEventListenerConfig.TOPIC_EXCHANGE, POST_CREATED_ROUTING_KEY, message);
    }
}
