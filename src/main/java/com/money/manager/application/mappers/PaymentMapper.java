package com.money.manager.application.mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.money.manager.domain.Payment;
import com.money.manager.domain.User;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

public class PaymentMapper {
    public static PaymentResponseDTO toDto(Payment payment) {
        return new PaymentResponseDTO(payment.getId(), payment.getPaymentDate().toString(), payment.getAmount());
    }

    public static Payment fromDto(PaymentRequestDTO paymentRequestDTO, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(paymentRequestDTO.paymentDate(), formatter);
        return Payment.builder().amount(paymentRequestDTO.amount()).paymentDate(date)
                .debt(DebtMapper.fromDto(paymentRequestDTO.debt(), user)).build();
    }
}
