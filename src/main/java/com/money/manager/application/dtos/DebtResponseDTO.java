package com.money.manager.application.dtos;

import com.money.manager.domain.paging.Page;

public record DebtResponseDTO(Long id, String name, Double totalAmount, String startDate, String endDate, Page<PaymentResponseDTO> payments) {
} 
