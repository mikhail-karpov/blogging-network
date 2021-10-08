package com.mikhailkarpov.bloggingnetwork.posts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
            .csrf()
                .disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/posts/**").authenticated()
                .and()
            .oauth2ResourceServer()
                .jwt();
        //@formatter:on
    }

    @Bean
    public JwtDecoder jwtDecoder(OAuth2ProviderProperties oAuth2ProviderProperties) {
        String serverUrl = oAuth2ProviderProperties.getServerUrl();
        String realm = oAuth2ProviderProperties.getRealm();
        return JwtDecoders.fromOidcIssuerLocation(String.format("%s/realms/%s", serverUrl, realm));
    }
}
