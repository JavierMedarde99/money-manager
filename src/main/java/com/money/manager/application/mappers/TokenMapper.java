package com.money.manager.application.mappers;

import com.money.manager.application.dtos.TokenResponseDTO;

public class TokenMapper {
    public static TokenResponseDTO toDto(String token){
        return new TokenResponseDTO("Bearer",token,15L);
    }
}
