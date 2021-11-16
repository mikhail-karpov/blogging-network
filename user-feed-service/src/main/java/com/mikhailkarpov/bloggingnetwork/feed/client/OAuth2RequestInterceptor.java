package com.mikhailkarpov.bloggingnetwork.feed.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

@RequiredArgsConstructor
public class OAuth2RequestInterceptor implements RequestInterceptor {

    private final DefaultClientCredentialsTokenResponseClient client =
            new DefaultClientCredentialsTokenResponseClient();

    private final OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest;

    public OAuth2RequestInterceptor(ClientRegistration clientRegistration) {
        this.clientCredentialsGrantRequest = new OAuth2ClientCredentialsGrantRequest(clientRegistration);
    }

    @Override
    public void apply(RequestTemplate template) {

        if (template.headers().containsKey("Authorization"))
            return;

        String tokenValue = this.client.getTokenResponse(this.clientCredentialsGrantRequest)
                .getAccessToken()
                .getTokenValue();

        template.header("Authorization", "Bearer " + tokenValue);
    }
}
