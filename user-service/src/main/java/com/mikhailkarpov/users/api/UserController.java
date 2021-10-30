package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<UserProfileDto> getById(@PathVariable String id) {

        return userService.findById(id)
                .map(profile -> ResponseEntity.ok(UserProfileDto.from(profile)))
                .orElse(ResponseEntity.notFound().build());
    }
}
