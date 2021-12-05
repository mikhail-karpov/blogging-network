package com.mikhailkarpov.users.service;

import com.mikhailkarpov.users.config.CacheConfig;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.util.DtoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CacheConfig.class)
@ImportAutoConfiguration({CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@ContextConfiguration
@TestPropertySource(properties = {
        "spring.cache.prefix=user-service",
        "spring.cache.expirations.users=60"})
@Testcontainers
class UserServiceCacheableImplIT {

    @TestConfiguration
    static class UserServiceConfig {

        @Bean
        public UserService delegate() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public UserService underTest(UserService delegate) {
            return new UserServiceCacheableImpl(delegate);
        }
    }

    @Container
    static GenericContainer REDIS = new GenericContainer("redis:latest").withExposedPorts(6379);

    @DynamicPropertySource
    private static void configRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired
    private UserService underTest;

    @Autowired
    private UserService delegate;

    @Autowired
    private CacheManager cacheManager;

    private final String id = UUID.randomUUID().toString();

    private final UserProfileDto expected = new UserProfileDto(id, "username");

    @BeforeEach
    void cleanUp() {
        Cache cache = getCache();
        cache.invalidate();

        Mockito.reset(this.delegate);
    }

    @Test
    void cacheShouldExists() {
        assertNotNull(this.cacheManager);
        assertTrue(this.cacheManager instanceof RedisCacheManager);
        assertNotNull(this.getCache());
    }

    @Test
    void givenRequest_whenCreateUser_thenCached() {
        //given
        UserRegistrationRequest request = DtoUtils.createRandomRequest();
        when(this.delegate.registerUser(request)).thenReturn(this.expected);

        //when
        UserProfileDto created = this.underTest.registerUser(request);
        UserProfileDto cached = findFromCacheById(id);
        Optional<UserProfileDto> found = this.underTest.findUserById(created.getId());

        //then
        assertEquals(this.expected, created);
        assertEquals(this.expected, cached);

        assertTrue(found.isPresent());
        assertEquals(this.expected, found.get());

        verify(this.delegate).registerUser(request);
        verifyNoMoreInteractions(this.delegate);
    }

    @Test
    void givenUser_whenFindById_thenFoundAndCached() {
        //given
        when(this.delegate.findUserById(this.id)).thenReturn(Optional.of(this.expected));

        //when
        Optional<UserProfileDto> found = this.underTest.findUserById(this.id);
        UserProfileDto cached = this.findFromCacheById(this.id);
        Optional<UserProfileDto> foundAgain = this.underTest.findUserById(this.id);

        //then
        assertTrue(found.isPresent());
        assertEquals(this.expected, found.get());
        assertEquals(this.expected, cached);

        verify(this.delegate, times(1)).findUserById(this.id);
    }

    @Test
    void givenNoFound_whenFindById_thenEmpty() {
        //given
        String userId = UUID.randomUUID().toString();
        when(this.delegate.findUserById(userId)).thenReturn(Optional.empty());

        //when
        Optional<UserProfileDto> found = this.underTest.findUserById(userId);
        UserProfileDto cached = this.findFromCacheById(userId);

        //then
        assertFalse(found.isPresent());
        assertNull(cached);
    }

    private Cache getCache() {
        return this.cacheManager.getCache(CacheConfig.USERS_CACHE);
    }

    private UserProfileDto findFromCacheById(String userId) {
        Cache cache = getCache();
        return cache.get(userId, UserProfileDto.class);
    }
}