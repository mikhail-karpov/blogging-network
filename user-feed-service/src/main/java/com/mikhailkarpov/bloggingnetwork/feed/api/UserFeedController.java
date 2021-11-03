package com.mikhailkarpov.bloggingnetwork.feed.api;

import com.mikhailkarpov.bloggingnetwork.feed.domain.PostActivity;
import com.mikhailkarpov.bloggingnetwork.feed.services.PostActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserFeedController {

    private final PostActivityService activityService;

    @GetMapping("/feed")
    public List<PostActivity> getUserFeed(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {

        String userId = jwt.getSubject();
        ArrayList<PostActivity> activities = new ArrayList<>();
        this.activityService.getFeed(userId, pageable).forEach(activities::add);

        return activities;
    }
}
