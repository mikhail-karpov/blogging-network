package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.model.Post;
import com.mikhailkarpov.bloggingnetwork.feed.services.UserFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserFeedController {

    private final UserFeedService userFeedService;

    @GetMapping("/feed")
    public List<Post> getUserFeed(@AuthenticationPrincipal Jwt jwt,
                                  @RequestParam(required = false, defaultValue = "0") int page,
                                  @RequestParam(required = false, defaultValue = "20") int size) {

        String userId = jwt.getSubject();
        return this.userFeedService.getUserFeed(userId, page, size);
    }
}
