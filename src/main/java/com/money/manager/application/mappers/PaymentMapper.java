package com.money.manager.application.mappers;

import com.money.manager.domain.Payment;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;

public class PaymentMapper {
    public static PaymentResponseDTO toDto(Payment payment){
        return new PaymentResponseDTO(payment.getId(), payment.getPaymentDate().toString(), payment.getAmount());
    }
}
