package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.dto.UserFeed;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserFeedController {

    private final UserFeedService userFeedService;

    @GetMapping("/feed")
    public UserFeed getUserFeed(@AuthenticationPrincipal Jwt jwt,
                                @RequestParam(required = false, defaultValue = "0") int page) {

        String userId = jwt.getSubject();
        return this.userFeedService.getUserFeed(userId, page);
    }
}
