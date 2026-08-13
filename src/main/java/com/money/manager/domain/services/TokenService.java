package com.money.manager.domain.services;

import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateToken(Authentication anAuthentication);
    String getUserFromToken(String token);
    boolean validateToken(String token);
    long getExpirationSeconds();
}
