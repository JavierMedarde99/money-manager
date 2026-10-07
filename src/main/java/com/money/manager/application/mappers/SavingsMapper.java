package com.money.manager.application.mappers;

import com.money.manager.application.dtos.SavingsResponseDTO;
import com.money.manager.domain.Savings;

public class SavingsMapper {

    public static SavingsResponseDTO toDto(Savings savings) {
        return new SavingsResponseDTO(
                savings.getYear(),
                savings.getMonth(),
                savings.getTotalIncome(),
                savings.getTotalExpense(),
                savings.getSavings());
    }
}
