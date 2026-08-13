package com.money.manager.application.dtos;

public record PaymentRequestDTO(String paymentDate, Double amount, DebtDTO debt) {
    
}
