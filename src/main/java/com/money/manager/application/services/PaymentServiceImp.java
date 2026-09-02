package com.money.manager.application.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
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
import com.money.manager.application.ports.DebtService;
import com.money.manager.application.ports.PaymentService;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final DebtService debtService;
    private final Clock clock;

    @Override
    @Transactional
    public PaymentResponseDTO insertPayment(PaymentRequestDTO paymentRequestDTO, User user) throws NotFoundException {
        Debt debt = debtRepository.findByIdAndUser_Id(paymentRequestDTO.debt().id(), user.getId())
                .orElseThrow(() -> new NotFoundException("debt not found"));
        Payment payment = PaymentMapper.fromDto(paymentRequestDTO, debt);
        Double totalPaid = debt.getPayments().stream().mapToDouble(Payment::getAmount).sum();
        if(totalPaid+ paymentRequestDTO.amount() >= debt.getTotalAmount()) {
            debtService.closeDebt(debt);
        }
        payment = paymentRepository.save(payment);

        if (Boolean.TRUE.equals(payment.getAutomaticPayment())) {
            backfillAutomaticPayments(payment, debt);
        }

        return PaymentMapper.toDto(payment);
    }

    private void backfillAutomaticPayments(Payment original, Debt debt) {
        LocalDate today = LocalDate.now(clock);
        YearMonth originalMonth = YearMonth.from(original.getPaymentDate());
        YearMonth currentMonth = YearMonth.from(today);

        if (!originalMonth.isBefore(currentMonth)) {
            return;
        }

        YearMonth cursor = originalMonth.plusMonths(1);
        while (!cursor.isAfter(currentMonth)) {
            LocalDate recurringDate = sameDayForMonth(original.getPaymentDate(), cursor);
            boolean alreadyExists = paymentRepository.existsByDebtAmountAndMonth(
                    debt, original.getAmount(),
                    recurringDate.getYear(), recurringDate.getMonthValue());
            if (!alreadyExists) {
                Payment saved = paymentRepository.save(Payment.builder()
                        .paymentDate(recurringDate)
                        .amount(original.getAmount())
                        .automaticPayment(true)
                        .debt(debt)
                        .build());
                closeDebtIfPaidOff(saved);
            }
            cursor = cursor.plusMonths(1);
        }
    }

    private void closeDebtIfPaidOff(Payment payment) {
        debtRepository.findById(payment.getDebt().getId()).ifPresent(debt -> {
            double totalPaid = debt.getPayments() == null ? 0.0
                    : debt.getPayments().stream().mapToDouble(Payment::getAmount).sum();
            if (totalPaid >= debt.getTotalAmount()) {
                debtService.closeDebt(debt);
            }
        });
    }

    private static LocalDate sameDayForMonth(LocalDate original, YearMonth target) {
        int day = Math.min(original.getDayOfMonth(), target.lengthOfMonth());
        return LocalDate.of(target.getYear(), target.getMonth(), day);
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
        payment.setAutomaticPayment(paymentRequestDTO.automaticPayment());
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
