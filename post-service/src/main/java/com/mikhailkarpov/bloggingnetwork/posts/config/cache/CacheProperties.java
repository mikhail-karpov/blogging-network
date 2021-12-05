package com.mikhailkarpov.bloggingnetwork.posts.config.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.cache")
@Validated
@Getter
@Setter
public class CacheProperties {

    @NotBlank
    private String prefix;

    @NotNull
    @NotEmpty
    private Map<String, Long> expirations;

}
