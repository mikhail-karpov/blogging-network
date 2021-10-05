package com.mikhailkarpov.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
            .csrf().disable()
            .authorizeRequests(auth -> auth
                    .antMatchers("/users/registration/**").permitAll()
                    .antMatchers("/users/login/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        //@formatter:on
    }

    @Bean
    public JwtDecoder jwtDecoder(KeycloakConfig keycloakConfig) {

        String issuerUri = String.format("%s/realms/%s", keycloakConfig.getServerUrl(), keycloakConfig.getRealm());
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
