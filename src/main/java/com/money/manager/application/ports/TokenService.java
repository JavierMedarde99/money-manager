package com.money.manager.application.ports;

public interface TokenService {
    String generateToken(String username);
    long getExpirationSeconds();
    String getUserFromToken(String token);
    boolean validateToken(String token);
}
