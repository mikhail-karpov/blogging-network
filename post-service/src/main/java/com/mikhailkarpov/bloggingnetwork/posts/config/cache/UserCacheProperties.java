package com.mikhailkarpov.bloggingnetwork.posts.config.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "spring.cache.users")
@Validated
@Getter
@Setter
public class UserCacheProperties {

    @NotBlank
    private String cacheName;

    @NotNull
    @Min(0)
    private Long timeToLive;
}
