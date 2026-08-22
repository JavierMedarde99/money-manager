package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DebtRequestDTO(
        @NotBlank String name,
        @Positive Double totalAmount,
        @NotBlank String startDate,
        String endDate) {
}
