package com.money.manager.infrastructure.dtos;

public record TransactionResponseDTO(String name, String transactionDate, Integer amount, Double price,
        String transactionType,
        String transactionSubtype, CategoryResponseDTO category, Long id) {

}
