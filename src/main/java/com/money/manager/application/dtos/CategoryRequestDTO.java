package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
        @NotBlank String name,
        @NotBlank String color) {
}
