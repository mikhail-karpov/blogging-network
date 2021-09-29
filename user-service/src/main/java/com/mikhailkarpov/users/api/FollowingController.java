package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserProfileDtoMapper;
import com.mikhailkarpov.users.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;
    private final UserProfileDtoMapper profileDtoMapper;

    @PostMapping("/users/{id}/followers")
    public void addToFollowers(@PathVariable("id") String userId, @RequestParam String followerId) {

        //todo extract followerId from JWT
        followingService.addToFollowers(userId, followerId);
    }

    @DeleteMapping("/users/{id}/followers")
    public void removeFromFollowers(@PathVariable("id") String userId, @RequestParam String followerId) {

        //todo extract followerId from JWT
        followingService.removeFromFollowers(userId, followerId);
    }

    @GetMapping("/users/{id}/followers")
    public PagedResult<UserProfileDto> getFollowers(@PathVariable("id") String userId, Pageable pageable) {

        Page<UserProfileDto> followersPage = followingService.findFollowers(userId, pageable)
                .map(profileDtoMapper::map);

        return new PagedResult<>(followersPage);
    }

    @GetMapping("/users/{id}/followings")
    public PagedResult<UserProfileDto> getFollowings(@PathVariable("id") String userId, Pageable pageable) {

        Page<UserProfileDto> followingsPage = followingService.findFollowings(userId, pageable)
                .map(profileDtoMapper::map);

        return new PagedResult<>(followingsPage);
    }
}
