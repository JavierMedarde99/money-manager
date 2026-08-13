package com.money.manager.application.dtos;

public record TransactionResponseDTO(Long id, String name, String transactionDate, Integer amount, Double price,
        String transactionType,
        String transactionSubtype, CategoryResponseDTO category) {

}
