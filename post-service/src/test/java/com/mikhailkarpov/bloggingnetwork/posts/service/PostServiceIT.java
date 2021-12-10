package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.config.AbstractIT;
import com.mikhailkarpov.bloggingnetwork.posts.dto.PostDto;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.messaging.PostMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration
class PostServiceIT extends AbstractIT {

    @TestConfiguration
    @RabbitListenerTest
    public static class TestConfig {

        @Component
        public class TestListener {

            private static final String LISTENER_ID = "post-event-listener";

            @RabbitListener(id = LISTENER_ID, queues = "post-event-queue")
            public void handle(PostMessage message) {
                //do nothing
            }
        }
    }

    @Autowired
    private PostService postService;

    @MockBean
    private UserService userService;

    @Autowired
    private RabbitListenerTestHarness harness;

    private TestConfig.TestListener listener;
    private LatchCountDownAndCallRealMethodAnswer answer;

    @BeforeEach
    void setUp() {
        when(this.userService.getUserById(any())).thenReturn(mock(UserProfileDto.class));

        this.listener = this.harness.getSpy(TestConfig.TestListener.LISTENER_ID);
        assertNotNull(this.listener);

        this.answer =
                this.harness.getLatchAnswerFor(TestConfig.TestListener.LISTENER_ID, 2);
        doAnswer(this.answer).when(this.listener).handle(any());
    }

    @AfterEach
    void verifyListener() throws InterruptedException {
        assertTrue(this.answer.await(30));
        verify(this.listener, times(2)).handle(any());
    }

    @Test
    void testLogic() throws InterruptedException {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        UUID postId = this.postService.createPost(userId, "post content");
        Optional<PostDto> foundPost = this.postService.findById(postId);
        Page<PostDto> postByUser = this.postService.findAllByUserId(userId, PageRequest.of(0, 2));
        this.postService.deleteById(postId);
        Optional<PostDto> notFoundPost = this.postService.findById(postId);

        //then
        assertTrue(foundPost.isPresent());
        assertEquals(1L, postByUser.getTotalElements());
        assertFalse(notFoundPost.isPresent());
    }
}
