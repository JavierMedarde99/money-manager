package com.money.manager.infrastructure.dtos;

public record TokenResponseDTO(String tokenType, String accessToken,Long expiresIn) {
    
}
