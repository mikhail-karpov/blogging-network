package com.mikhailkarpov.bloggingnetwork.posts.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@RequiredArgsConstructor
public class OAuth2Client {

    private final String serverUrl;
    private final String realm;
    private final RestTemplate restTemplate;

    public String obtainAccessToken(OAuth2User oAuth2User) {
        MultiValueMap<Object, Object> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", oAuth2User.getUsername());
        params.add("password", oAuth2User.getPassword());
        params.add("client_id", "admin-cli");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<AccessTokenResponse> exchange =
                restTemplate.exchange(getTokenUrl(), POST, new HttpEntity<>(params, headers), AccessTokenResponse.class);

        assertThat(exchange.getStatusCodeValue()).isEqualTo(200);
        assertThat(exchange.getBody()).isNotNull();

        return exchange.getBody().accessToken;
    }

    private String getTokenUrl() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
    }

    @Value
    private static class AccessTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        @JsonProperty("scope")
        private String scope;
    }
}
