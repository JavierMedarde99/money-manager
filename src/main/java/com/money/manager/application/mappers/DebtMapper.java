package com.money.manager.application.mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.money.manager.domain.Debt;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.DebtDTO;
import com.money.manager.infrastructure.dtos.DebtRequestDTO;
import com.money.manager.infrastructure.dtos.DebtResponseDTO;

public class DebtMapper {
    public static Debt fromDto(DebtRequestDTO debtRequestDTO, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate dateStart = LocalDate.parse(debtRequestDTO.starDate(), formatter);
        LocalDate dateEnd = debtRequestDTO.endDate() == null ? null
                : LocalDate.parse(debtRequestDTO.endDate(), formatter);
        return Debt.builder().name(debtRequestDTO.name()).totalAmount(debtRequestDTO.totalAmount())
                .startDate(dateStart)
                .endDate(dateEnd).user(user).build();
    }

    public static Debt fromDto(DebtDTO debtResponseDTO, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate dateStart = LocalDate.parse(debtResponseDTO.starDate(), formatter);
        LocalDate dateEnd = debtResponseDTO.endDate() == null ? null
                : LocalDate.parse(debtResponseDTO.endDate(), formatter);
        return Debt.builder().name(debtResponseDTO.name()).totalAmount(debtResponseDTO.totalAmount())
                .startDate(dateStart)
                .endDate(dateEnd).user(user).id(debtResponseDTO.id()).build();
    }

    public static DebtResponseDTO toDto(Debt debt) {
        return new DebtResponseDTO(debt.getId(), debt.getName(), debt.getTotalAmount(), debt.getStartDate().toString(),
                debt.getEndDate() == null ? null : debt.getEndDate().toString(),
                debt.getPayments().isEmpty() ? null
                        : debt.getPayments().stream().map(payment -> PaymentMapper.toDto(payment)).toList());
    }
}
