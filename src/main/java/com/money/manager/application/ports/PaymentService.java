package com.money.manager.application.ports;

import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;

public interface PaymentService {
    PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user);
    PaymentResponseDTO getPayment(Long id) throws NotFoundException;
    PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO,Long id) throws NotFoundException;
    String deletePayment(Long id) throws NotFoundException;
}
