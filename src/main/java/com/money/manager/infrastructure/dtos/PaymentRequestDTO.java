package com.money.manager.infrastructure.dtos;

public record PaymentRequestDTO(String paymentDate, Double amount, DebtDTO debt) {
    
}
