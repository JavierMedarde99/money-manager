package com.money.manager.application.ports;

import com.money.manager.domain.Payment;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user) throws NotFoundException;
    PaymentResponseDTO getPayment(Long id, User user) throws NotFoundException;
    PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO,Long id, User user) throws NotFoundException;
    String deletePayment(Long id, User user) throws NotFoundException;
    Transaction createExpenseTransaction(Payment payment, User user);
}
