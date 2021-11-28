package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.UserAuthenticationRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.UserRegistrationRequest;
import com.mikhailkarpov.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @PostMapping("/account/registration")
    public ResponseEntity<UserProfileDto> create(@Valid @RequestBody UserRegistrationRequest request,
                                                 UriComponentsBuilder uriComponentsBuilder) {

        UserProfileDto profile = userService.create(request);
        URI location = uriComponentsBuilder.path("/account/profile").build().toUri();

        return ResponseEntity.created(location).body(profile);
    }

    @PostMapping("/account/login")
    public AccessTokenResponse login(@Valid @RequestBody UserAuthenticationRequest request) {

        return userService.authenticate(request);
    }

    @GetMapping("/account/profile")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        return this.userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
