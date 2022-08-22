package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.client.UserServiceClient;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.excepition.ResourceNotFoundException;
import com.mikhailkarpov.bloggingnetwork.posts.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void givenUserFound_whenGetById_thenOk() {
        //given
        String userId = "user-id";
        String username = "username";
        UserProfileDto profile = new UserProfileDto(userId, username);
        Mockito.when(this.userServiceClient.findById(userId)).thenReturn(Optional.of(profile));

        //when
        UserProfileDto found = this.userService.getUserById(userId);

        //then
        assertThat(found).hasNoNullFieldsOrProperties();
        assertThat(found).isEqualTo(profile);
    }

    @Test
    void givenUserNotFound_whenGetById_thenExceptionIsThrown() {
        //given
        String userId = "user-id";
        Mockito.when(this.userServiceClient.findById(userId)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> this.userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}