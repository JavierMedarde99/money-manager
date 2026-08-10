package com.money.manager.application.services;

import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.PaymentMapper;
import com.money.manager.domain.Payment;
import com.money.manager.domain.User;
import com.money.manager.domain.services.PaymentService;
import com.money.manager.infrastructure.dtos.PaymentRequestDTO;
import com.money.manager.infrastructure.dtos.PaymentResponseDTO;
import com.money.manager.infrastructure.persistance.PostgresPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService{

    private final PostgresPaymentRepository paymentRepository;

    @Override
    public PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO,User user) {
        Payment payment = PaymentMapper.fromDto(paymentRequestDTO, user);
        payment = paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDTO getPayment(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPayment'");
    }

    @Override
    public PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePayment'");
    }

    @Override
    public String deletePayment(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePayment'");
    }
    
}
