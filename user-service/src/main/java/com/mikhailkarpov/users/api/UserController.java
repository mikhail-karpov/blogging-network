package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.domain.UserProfile;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserProfileDtoMapper;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserProfileDtoMapper profileDtoMapper;

    @PostMapping
    public ResponseEntity<UserProfileDto> create(@Valid @RequestBody UserRegistrationRequest request,
                                                 UriComponentsBuilder uriComponentsBuilder) {

        UserProfile profile = userService.create(request);
        URI location = uriComponentsBuilder.path("/users/{id}").build(profile.getId());

        return ResponseEntity.created(location).body(profileDtoMapper.map(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getById(@PathVariable String id) {

        return userService.findById(id)
                .map(profile -> ResponseEntity.ok(profileDtoMapper.map(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

}
