package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransactionRequestDTO(
        @NotBlank @Size(max = 255) String name,
        @NotBlank String transactionDate,
        @Positive Integer amount,
        @Positive Double price,
        @NotBlank String transactionType,
        @NotBlank String transactionSubtype,
        @NotNull CategoryResponseDTO category) {
}
