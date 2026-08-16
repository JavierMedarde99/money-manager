package com.money.manager.application.dtos;

public record TokenResponseDTO(String tokenType, String accessToken,Long expiresIn) {
    
}
