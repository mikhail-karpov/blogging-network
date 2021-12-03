package com.mikhailkarpov.bloggingnetwork.posts.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikhailkarpov.bloggingnetwork.posts.config.SecurityTestConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.CreateCommentRequest;
import com.mikhailkarpov.bloggingnetwork.posts.service.CommentService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
@ContextConfiguration(classes = SecurityTestConfig.class)
class CommentControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @ParameterizedTest
    @NullSource
    @MethodSource("getInvalidCommentRequest")
    void givenInvalidRequest_whenPostComment_thenBadRequest(CreateCommentRequest request) throws Exception {
        //given
        UUID postId = UUID.randomUUID();

        //when
        this.mockMvc.perform(post("/posts/{id}/comments", postId)
                        .with(jwt())
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(this.commentService);
    }

    private static Stream<Arguments> getInvalidCommentRequest() {
        return Stream.of(
                Arguments.of(new CreateCommentRequest(null)),
                Arguments.of(new CreateCommentRequest("")),
                Arguments.of(new CreateCommentRequest(RandomStringUtils.randomAlphabetic(3))),
                Arguments.of(new CreateCommentRequest(RandomStringUtils.randomAlphabetic(181)))
        );
    }
}