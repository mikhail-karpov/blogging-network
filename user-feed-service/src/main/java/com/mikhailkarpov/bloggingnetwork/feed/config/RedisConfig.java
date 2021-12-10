package com.mikhailkarpov.bloggingnetwork.feed.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories("com.mikhailkarpov.bloggingnetwork.feed.repository")
public class RedisConfig {
}
