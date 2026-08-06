package com.money.manager.infrastructure.dtos;

public record LoginRequestDTO(
    String username,
    String password
) {
    
}
