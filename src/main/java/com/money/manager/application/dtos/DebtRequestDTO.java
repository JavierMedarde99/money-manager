package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DebtRequestDTO(
        @NotBlank @Size(max = 255) String name,
        @NotNull @Positive Double totalAmount,
        @NotBlank String startDate,
        String endDate) {
}
