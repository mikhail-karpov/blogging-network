package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.exception.ResourceAlreadyExistsException;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceImplIT extends AbstractIT {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void givenRequest_whenCreateUser_thenCreatedAndCached() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        UserProfile createdProfile = userService.create(request);
        Optional<UserProfile> cachedProfile = fromCache(createdProfile.getId());

        //then
        assertThat(createdProfile.getUsername()).isEqualTo(request.getUsername());
        assertThat(createdProfile.getEmail()).isEqualTo(request.getEmail());
        assertThat(cachedProfile).isPresent();
        assertThat(cachedProfile.get()).isEqualTo(createdProfile);
    }

    @Test
    void givenDuplicateRequest_whenCreateUser_thenThrown() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //then
        UserProfile createdProfile = userService.create(request);
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.create(request));
    }

    @Test
    void givenRequest_whenCreateUserAndGetById_thenFound() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        UserProfile createdProfile = userService.create(request);
        Optional<UserProfile> foundProfile = userService.findById(createdProfile.getId());

        //then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get()).isEqualTo(createdProfile);
    }

    @Test
    void givenNoUserExists_whenFindById_thenEmpty() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        Optional<UserProfile> foundProfile = userService.findById(userId);
        Optional<UserProfile> cachedProfile = fromCache(userId);

        //then
        assertThat(foundProfile).isEmpty();
        assertThat(cachedProfile).isEmpty();
    }

    @Test
    void givenCreatedUsers_whenFindById_thenFound() {
        //given
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UserProfile userProfile = userService.create(DtoUtils.createRandomRequest());
            idList.add(userProfile.getId());
        }

        //when
        List<String> foundIdList = new ArrayList<>(5);
        for (UserProfile userProfile : userService.findByIdIn(idList)) {
            foundIdList.add(userProfile.getId());
        }

        //then
        assertThat(foundIdList.size()).isEqualTo(5);
    }

    private Optional<UserProfile> fromCache(String userId) {

        Cache cache = cacheManager.getCache(UserServiceImpl.USER_PROFILE_CACHE);
        return Optional.ofNullable(cache).map(c -> c.get(userId, UserProfile.class));
    }
}