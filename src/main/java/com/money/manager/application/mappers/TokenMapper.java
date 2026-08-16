package com.money.manager.application.mappers;

import com.money.manager.infrastructure.dtos.TokenResponseDTO;

public class TokenMapper {
    public static TokenResponseDTO toDto(String token, long expiresInSeconds){
        return new TokenResponseDTO("Bearer",token,expiresInSeconds);
    }
}
