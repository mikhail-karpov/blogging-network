package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.dto.Post;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserFeedController {

    private final UserFeedService userFeedService;

    @GetMapping("/feed")
    public List<Post> getUserFeed(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {

        String userId = jwt.getSubject();
        return this.userFeedService.getUserFeed(userId, pageable);
    }
}
