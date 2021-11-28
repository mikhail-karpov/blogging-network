package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceCacheIT extends AbstractIT {

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void givenRequest_whenCreateUser_thenCreatedAndCached() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        UserProfileDto createdProfile = this.userService.create(request);
        Optional<UserProfileDto> cachedProfile = findFromCacheById(createdProfile.getId());

        //then
        assertThat(cachedProfile).isPresent();
        assertThat(cachedProfile.get()).isEqualTo(createdProfile);
    }

    @Test
    void givenUser_whenFindById_thenFoundAndCached() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();

        //when
        UserProfileDto created = this.userService.create(request);
        Optional<UserProfileDto> found = this.userService.findById(created.getId());
        Optional<UserProfileDto> cached = findFromCacheById(created.getId());

        //then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(created);

        assertThat(cached).isPresent();
        assertThat(cached.get()).isEqualTo(created);
    }

    @Test
    void givenNoUserExists_whenFindById_thenEmpty() {
        //given
        String userId = UUID.randomUUID().toString();

        //then
        assertThat(this.userService.findById(userId)).isEmpty();
        assertThat(findFromCacheById(userId)).isEmpty();
    }

    private Optional<UserProfileDto> findFromCacheById(String userId) {

        Cache cache = this.cacheManager.getCache(UserServiceImpl.USER_PROFILE_CACHE);
        return Optional.ofNullable(cache).map(c -> c.get(userId, UserProfileDto.class));
    }
}