package com.mikhailkarpov.bloggingnetwork.posts.client;

import com.mikhailkarpov.bloggingnetwork.posts.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@FeignClient(name = "user-service", decode404 = true, fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}/profile", consumes = "application/json")
    Optional<UserProfileDto> findById(@PathVariable("id") String userId);
}
