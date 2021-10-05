package com.mikhailkarpov.users.util;

import com.fasterxml.jackson.annotation.JsonProperty;
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

public class OAuth2Utils {

    private final RestTemplate restTemplate;

    private final String serverUrl;

    private final String realm;

    public OAuth2Utils(RestTemplate restTemplate, String serverUrl, String realm) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
        this.realm = realm;
    }

    public String obtainAccessToken(String username, String password) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);
        params.add("client_id", "admin-cli");

        HttpHeaders headers = new HttpHeaders(params);
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
