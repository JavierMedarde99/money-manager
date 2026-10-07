package com.money.manager.application.dtos;

public record SavingsResponseDTO(
        int year,
        int month,
        double totalIncome,
        double totalExpense,
        double savings) {
}
