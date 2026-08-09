package com.money.manager.infrastructure.dtos;

public record TransactionRequestDTO(String name, String transactionDate, Integer amount, Double price,
        String transactionType,
        String transactionSubtype, CategoryResponseDTO category) {

}
