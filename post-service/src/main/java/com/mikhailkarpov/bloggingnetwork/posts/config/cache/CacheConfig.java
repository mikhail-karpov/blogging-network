package com.mikhailkarpov.bloggingnetwork.posts.config.cache;

import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    public static final String USER_CACHE = "users";

    @Autowired
    private CacheProperties cacheProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(this.cacheProperties.getPrefix())
                .disableCachingNullValues()
                .serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(UserProfileDto.class)))
                .entryTtl(Duration.ofMinutes(this.cacheProperties.getExpirations().get(USER_CACHE)));

        return RedisCacheManager.builder()
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .withCacheConfiguration(USER_CACHE, cacheConfiguration)
                .build();
    }
}
