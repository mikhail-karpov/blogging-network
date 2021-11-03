package com.mikhailkarpov.bloggingnetwork.feed.messaging;

import com.mikhailkarpov.bloggingnetwork.feed.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.feed.config.JwtDecoderTestConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.FollowingEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.config.messaging.PostEventListenerConfig;
import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.FOLLOWED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.FollowingEvent.Status.UNFOLLOWED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.CREATED;
import static com.mikhailkarpov.bloggingnetwork.feed.messaging.PostEvent.Status.DELETED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MessagingIT extends AbstractIT {

    @Autowired
    private PostActivityService postActivityService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void testUserFeed() {
        //given
        sendPostEvent("user-id", "post1", CREATED);
        sendPostEvent("user-id", "post2", CREATED);
        sendPostEvent("user2-id", "post3", CREATED);
        sendPostEvent("user2-id", "post4", CREATED);

        sendFollowingEvent("follower-id", "user2-id", FOLLOWED);
        sendFollowingEvent("follower-id", "user-id", FOLLOWED);

        //when
        Awaitility.await()
                .atMost(60L, TimeUnit.SECONDS)
                .until(() -> getUserFeed("follower-id", PageRequest.of(0, 4)).size() == 4);

        //then
        assertThat(getUserFeed("follower-id", PageRequest.of(1, 3)).size()).isEqualTo(1);
    }

    private void sendPostEvent(String authorId, String postId, PostEvent.Status status) {
        String topicExchange = PostEventListenerConfig.TOPIC_EXCHANGE;
        String createdRoutingKey = PostEventListenerConfig.POST_CREATED_ROUTING_KEY;
        String deletedRoutingKey = PostEventListenerConfig.POST_DELETED_ROUTING_KEY;

        PostEvent event = new PostEvent(postId, authorId, status);

        if (CREATED == status) {
            this.rabbitTemplate.convertAndSend(topicExchange, createdRoutingKey, event);

        } else if (DELETED == status) {
            this.rabbitTemplate.convertAndSend(topicExchange, deletedRoutingKey, event);
        }
    }

    private void sendFollowingEvent(String followerUserId, String followingUserId, FollowingEvent.Status status) {
        String topicExchange = FollowingEventListenerConfig.TOPIC_EXCHANGE;
        String followKey = FollowingEventListenerConfig.FOLLOW_ROUTING_KEY;
        String unfollowKey = FollowingEventListenerConfig.UNFOLLOW_ROUTING_KEY;

        FollowingEvent event = new FollowingEvent(followerUserId, followingUserId, status);

        if (FOLLOWED == status) {
            this.rabbitTemplate.convertAndSend(topicExchange, followKey, event);

        } else if (UNFOLLOWED == status) {
            this.rabbitTemplate.convertAndSend(topicExchange, unfollowKey, event);
        }
    }

    private List<PostActivity> getUserFeed(String userId, Pageable pageable) {
        List<PostActivity> activities = new ArrayList<>();
        this.postActivityService.getFeed(userId, pageable)
                .forEach(activities::add);
        return activities;
    }
}
