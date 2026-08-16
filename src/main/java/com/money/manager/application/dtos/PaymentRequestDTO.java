package com.money.manager.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequestDTO(
        @NotBlank String paymentDate,
        @Positive Double amount,
        @NotNull DebtDTO debt) {
}
