package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.infrastructure.dtos.PaymentRequestDTO;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user);
    PaymentResponseDTO getPayment(Long id) throws NotFoundException;
    PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO,Long id) throws NotFoundException;
    String deletePayment(Long id) throws NotFoundException;
}
