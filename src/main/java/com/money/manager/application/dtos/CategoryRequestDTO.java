package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color) {
}
