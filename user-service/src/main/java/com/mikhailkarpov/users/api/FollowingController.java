package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;

    @PostMapping("/users/{id}/followers")
    public void follow(@PathVariable("id") String userId, @AuthenticationPrincipal Jwt jwt) {

        String followerId = jwt.getSubject();
        this.followingService.addToFollowers(userId, followerId);
    }

    @DeleteMapping("/users/{id}/followers")
    public void unfollow(@PathVariable("id") String userId, @AuthenticationPrincipal Jwt jwt) {

        String followerId = jwt.getSubject();
        this.followingService.removeFromFollowers(userId, followerId);
    }

    @GetMapping("/users/{id}/followers")
    public PagedResult<UserProfileDto> getFollowers(@PathVariable("id") String userId, Pageable pageable) {

        Page<UserProfileDto> followersPage = this.followingService.findFollowers(userId, pageable);
        return new PagedResult<>(followersPage);
    }

    @GetMapping("/users/{id}/following")
    public PagedResult<UserProfileDto> getFollowings(@PathVariable("id") String userId, Pageable pageable) {

        Page<UserProfileDto> followingPage = followingService.findFollowing(userId, pageable);
        return new PagedResult<>(followingPage);
    }
}
