package com.money.manager.infrastructure.dtos;

import java.util.List;

public record DebtResponseDTO(Long id, String name, Double totalAmount, String starDate, String endDate,List<PaymentResponseDTO> payments) {
} 
