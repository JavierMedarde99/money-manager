package com.money.manager.application.ports;

public interface AuthenticationPort {

    /**
     * Authenticates the given credentials using the application security configuration.
     *
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    void authenticate(String username, String password);
}
