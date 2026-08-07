package com.money.manager.infrastructure.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(@NotBlank String username,@NotBlank String password,@NotBlank String email) {
    
}
