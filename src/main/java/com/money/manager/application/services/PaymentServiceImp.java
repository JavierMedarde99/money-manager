package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.PaymentMapper;
import com.money.manager.domain.Payment;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.ports.PaymentService;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;
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
    public PaymentResponseDTO getPayment(Long id) throws NotFoundException{
        Payment payment = findById(id);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO,Long id) throws NotFoundException{
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Payment payment = findById(id);
        payment.setAmount(paymentRequestDTO.amount());
        payment.setPaymentDate(LocalDate.parse(paymentRequestDTO.paymentDate(), formatter));
        paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public String deletePayment(Long id) throws NotFoundException{
        Payment payment = findById(id);
        paymentRepository.delete(payment);
        return "Payment delete";
    }
    
    private Payment findById(Long id) throws NotFoundException{
        Optional<Payment> optPayment = paymentRepository.findById(id);
        return optPayment.orElseThrow(()-> new NotFoundException());
    }
}
