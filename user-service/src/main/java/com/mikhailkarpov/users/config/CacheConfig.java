package com.mikhailkarpov.users.config;

import com.mikhailkarpov.users.dto.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    public static final String USERS_CACHE = "users";

    @Autowired
    private CacheProperties userCacheProperties;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(this.userCacheProperties.getPrefix())
                .disableCachingNullValues()
                .serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(UserProfileDto.class)))
                .entryTtl(Duration.ofMinutes(this.userCacheProperties.getExpirations().get(USERS_CACHE)));

        return RedisCacheManager.builder()
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .withCacheConfiguration(USERS_CACHE, cacheConfiguration)
                .build();
    }
}
