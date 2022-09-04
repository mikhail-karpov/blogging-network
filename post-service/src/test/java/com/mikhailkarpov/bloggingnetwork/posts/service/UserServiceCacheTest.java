package com.mikhailkarpov.bloggingnetwork.posts.service;

import com.mikhailkarpov.bloggingnetwork.posts.client.UserServiceClient;
import com.mikhailkarpov.bloggingnetwork.posts.config.cache.CacheConfig;
import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import com.mikhailkarpov.bloggingnetwork.posts.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
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
@Import({CacheConfig.class, UserServiceImpl.class})
@ImportAutoConfiguration(classes = {CacheAutoConfiguration.class, RedisAutoConfiguration.class})
@TestPropertySource(properties = {
        "spring.cache.prefix=post-service",
        "spring.cache.expirations.users=60"
})
@Testcontainers
public class UserServiceCacheTest {

    @Container
    static GenericContainer REDIS = new GenericContainer("redis:latest").withExposedPorts(6379);

    @DynamicPropertySource
    static void configRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", REDIS::getFirstMappedPort);
    }

    @MockBean
    private UserServiceClient userServiceClient;

    @Autowired
    private UserService userServiceImpl;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void contextLoads() {
        assertNotNull(this.cacheManager);
        assertTrue(this.cacheManager instanceof RedisCacheManager);
        assertNotNull(this.getUserCache());
    }

    @Test
    void givenUser_whenGetUser_thenFoundAndCached() {
        //given
        String userId = UUID.randomUUID().toString();
        UserProfileDto user = new UserProfileDto(userId, "user-profile");

        when(this.userServiceClient.findById(userId)).thenReturn(Optional.of(user));

        //when
        UserProfileDto userCacheMiss = this.userServiceImpl.getUserById(userId);
        UserProfileDto userCacheHit = this.userServiceImpl.getUserById(userId);

        //then
        verify(this.userServiceClient, times(1)).findById(userId);

        assertEquals(user, userCacheMiss);
        assertEquals(user, userCacheHit);
        assertEquals(user, this.getFromCacheById(userId));
    }

    private Cache getUserCache() {
        return this.cacheManager.getCache(CacheConfig.USER_CACHE);
    }

    private Object getFromCacheById(String userId) {
        return this.getUserCache().get(userId).get();
    }
}
