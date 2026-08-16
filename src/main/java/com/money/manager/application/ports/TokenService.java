package com.money.manager.application.ports;

import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateToken(Authentication anAuthentication);
    String getUserFromToken(String token);
    boolean validateToken(String token);
    long getExpirationSeconds();
}
