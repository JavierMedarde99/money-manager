package com.money.manager.application.dtos;

import java.util.List;

public record DebtResponseDTO(Long id, String name, Double totalAmount, String starDate, String endDate,List<PaymentResponseDTO> payments) {
} 
