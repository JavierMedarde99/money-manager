package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.infrastructure.dtos.PaymentRequestDTO;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user) throws NotFoundException;
    PaymentResponseDTO getPayment(Long id, User user) throws NotFoundException;
    PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO,Long id, User user) throws NotFoundException;
    String deletePayment(Long id, User user) throws NotFoundException;
}
