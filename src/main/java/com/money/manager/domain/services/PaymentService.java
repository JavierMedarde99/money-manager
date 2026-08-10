package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.infrastructure.dtos.PaymentRequestDTO;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user);
    PaymentResponseDTO getPayment(Long id);
    PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO);
    String deletePayment(Long id);
}
