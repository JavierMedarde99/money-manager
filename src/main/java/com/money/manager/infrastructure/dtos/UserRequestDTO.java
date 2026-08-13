package com.money.manager.infrastructure.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(@NotBlank String username, String password,
        @NotBlank @Email String email) {
}
