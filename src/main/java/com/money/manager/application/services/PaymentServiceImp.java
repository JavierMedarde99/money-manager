package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.application.mappers.PaymentMapper;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.ports.PaymentService;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;

    @Override
    @Transactional
    public PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO, User user) throws NotFoundException {
        Debt debt = debtRepository.findByIdAndUser_Id(paymentRequestDTO.debt().id(), user.getId())
                .orElseThrow(() -> new NotFoundException("debt not found"));
        Payment payment = PaymentMapper.fromDto(paymentRequestDTO, debt);
        payment = paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDTO getPayment(Long id, User user) throws NotFoundException{
        Payment payment = findById(id, user);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDTO updatePayment(PaymentRequestDTO paymentRequestDTO, Long id, User user) throws NotFoundException{
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Payment payment = findById(id, user);
        payment.setAmount(paymentRequestDTO.amount());
        payment.setPaymentDate(LocalDate.parse(paymentRequestDTO.paymentDate(), formatter));
        paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Override
    public String deletePayment(Long id, User user) throws NotFoundException{
        Payment payment = findById(id, user);
        paymentRepository.delete(payment);
        return "Payment delete";
    }
    
    private Payment findById(Long id, User user) throws NotFoundException{
        Optional<Payment> optPayment = paymentRepository.findByIdAndDebt_User_Id(id, user.getId());
        return optPayment.orElseThrow(()-> new NotFoundException());
    }
}
