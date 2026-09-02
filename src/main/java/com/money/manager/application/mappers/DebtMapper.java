package com.money.manager.application.mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.money.manager.domain.Debt;
import com.money.manager.domain.User;
import com.money.manager.domain.paging.Page;
import com.money.manager.application.dtos.DebtDTO;
import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

public class DebtMapper {
    public static Debt fromDto(DebtRequestDTO debtRequestDTO, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate dateStart = LocalDate.parse(debtRequestDTO.startDate(), formatter);
        LocalDate dateEnd = debtRequestDTO.endDate() == null ? null
                : LocalDate.parse(debtRequestDTO.endDate(), formatter);
        return Debt.builder().name(debtRequestDTO.name()).totalAmount(debtRequestDTO.totalAmount())
                .startDate(dateStart)
                .endDate(dateEnd).user(user).build();
    }

    public static Debt fromDto(DebtDTO debtResponseDTO, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate dateStart = LocalDate.parse(debtResponseDTO.startDate(), formatter);
        LocalDate dateEnd = debtResponseDTO.endDate() == null ? null
                : LocalDate.parse(debtResponseDTO.endDate(), formatter);
        return Debt.builder().name(debtResponseDTO.name()).totalAmount(debtResponseDTO.totalAmount())
                .startDate(dateStart)
                .endDate(dateEnd).user(user).id(debtResponseDTO.id()).build();
    }

    public static DebtResponseDTO toDto(Debt debt, Page<PaymentResponseDTO> payments) {
        return new DebtResponseDTO(debt.getId(), debt.getName(), debt.getTotalAmount(), debt.getStartDate().toString(),
                debt.getEndDate() == null ? null : debt.getEndDate().toString(),
                payments);
    }
}
