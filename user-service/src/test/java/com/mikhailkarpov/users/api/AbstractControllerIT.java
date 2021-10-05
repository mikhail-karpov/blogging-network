package com.mikhailkarpov.users.api;

import com.mikhailkarpov.users.config.AbstractIT;
import com.mikhailkarpov.users.util.OAuth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

public abstract class AbstractControllerIT extends AbstractIT {

    @Autowired
    private OAuth2Utils oAuth2Utils;

    public HttpHeaders buildAuthHeader(String username, String password) {
        String accessToken = oAuth2Utils.obtainAccessToken(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        return headers;
    }
}
