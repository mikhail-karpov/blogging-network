package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.dto.AccessTokenDto;
import com.mikhailkarpov.users.dto.SignInRequest;
import com.mikhailkarpov.users.dto.UserProfileDto;
import com.mikhailkarpov.users.dto.SignUpRequest;
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

    @PostMapping("/account/signUp")
    public ResponseEntity<UserProfileDto> signUp(@Valid @RequestBody SignUpRequest request,
                                                 UriComponentsBuilder uriComponentsBuilder) {

        UserProfileDto profile = userService.signUp(request);
        URI location = uriComponentsBuilder.path("/account/profile").build().toUri();

        return ResponseEntity.created(location).body(profile);
    }

    @PostMapping("/account/signIn")
    public AccessTokenDto signIn(@Valid @RequestBody SignInRequest request) {

        return userService.signIn(request);
    }

    @GetMapping("/account/profile")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        return this.userService.findUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
