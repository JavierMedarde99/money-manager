package com.money.manager.infrastructure.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank
    String username,
    @NotBlank
    String password
) {
    
}
