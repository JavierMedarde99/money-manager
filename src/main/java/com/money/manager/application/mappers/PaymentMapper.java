package com.money.manager.application.mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.money.manager.domain.Debt;
import com.money.manager.domain.Payment;
import com.money.manager.infrastructure.dtos.PaymentRequestDTO;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;

public class PaymentMapper {
    public static PaymentResponseDTO toDto(Payment payment) {
        return new PaymentResponseDTO(payment.getId(), payment.getPaymentDate().toString(), payment.getAmount());
    }

    public static Payment fromDto(PaymentRequestDTO paymentRequestDTO, Debt debt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(paymentRequestDTO.paymentDate(), formatter);
        return Payment.builder().amount(paymentRequestDTO.amount()).paymentDate(date)
                .debt(debt).build();
    }
}
