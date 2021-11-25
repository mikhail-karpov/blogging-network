package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.domain.UserProfileIntf;
import com.mikhailkarpov.users.dto.PagedResult;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<UserProfileDto> findById(@PathVariable String id) {

        return this.userService.findById(id)
                .map(profile -> new UserProfileDto(profile.getId(), profile.getUsername()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/search")
    public PagedResult<UserProfileDto> findByUsernameLike(@RequestParam String username, Pageable pageable) {

        Page<UserProfileDto> profiles = this.userService.findByUsernameLike(username, pageable)
                .map(profile -> new UserProfileDto(profile.getId(), profile.getUsername()));

        return new PagedResult<>(profiles);
    }
}
