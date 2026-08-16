package com.money.manager.infrastructure.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.money.manager.application.ports.AuthenticationPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuthenticationPort implements AuthenticationPort {

    private final AuthenticationConfiguration authenticationConfiguration;

    @Override
    public void authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        try {
            authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials", e);
        }
    }
}
