package com.money.manager.application.ports;

public interface TokenService {
    String generateToken(String username);
    String getUserFromToken(String token);
    boolean validateToken(String token);
}
