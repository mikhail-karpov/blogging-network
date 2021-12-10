package com.mikhailkarpov.bloggingnetwork.feed.client;

import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@FeignClient(name = "post-service", decode404 = true, fallback = PostServiceClientFallback.class)
public interface PostServiceClient {

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    Optional<Post> getPostById(@PathVariable("id") String postId);

}
