package com.money.manager.application.services;

import java.time.Clock;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.application.ports.DebtService;
import com.money.manager.application.ports.PaymentService;
import com.money.manager.application.ports.RecurringService;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.enums.Subtype;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecurringServiceImp implements RecurringService {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final DebtService debtService;
    private final PaymentService paymentService;
    private final Clock clock;

    @Override
    @Transactional
    public void processFixedTransactions() {
        LocalDate today = LocalDate.now(clock);
        for (Transaction transaction : transactionRepository.findBySubtype(Subtype.FIXED)) {
            LocalDate recurringDate = sameDayForMonth(transaction.getDateTransaction(), today);
            boolean alreadyExists = transactionRepository.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                    transaction.getUser(), transaction.getCategory(), transaction.getName(),
                    transaction.getAmount(), transaction.getType(), transaction.getSubtype(),
                    recurringDate.getYear(), recurringDate.getMonthValue());
            if (!alreadyExists) {
                transactionRepository.save(Transaction.builder()
                        .name(transaction.getName())
                        .dateTransaction(recurringDate)
                        .amount(transaction.getAmount())
                        .price(transaction.getPrice())
                        .type(transaction.getType())
                        .subtype(transaction.getSubtype())
                        .user(transaction.getUser())
                        .category(transaction.getCategory())
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void processAutomaticPayments() {
        LocalDate today = LocalDate.now(clock);
        Set<AbstractMap.SimpleEntry<Long, Double>> processed = new HashSet<>();
        for (Payment payment : paymentRepository.findAutomaticPaymentsForOpenDebts()) {
            Long debtId = payment.getDebt() != null ? payment.getDebt().getId() : null;
            Double amount = payment.getAmount();
            if (debtId == null || amount == null) {
                continue;
            }
            AbstractMap.SimpleEntry<Long, Double> key = new AbstractMap.SimpleEntry<>(debtId, amount);
            if (processed.contains(key)) {
                continue;
            }
            LocalDate recurringDate = sameDayForMonth(payment.getPaymentDate(), today);
            boolean alreadyExists = paymentRepository.existsByDebtAmountAndMonth(
                    payment.getDebt(), amount,
                    recurringDate.getYear(), recurringDate.getMonthValue());
            if (alreadyExists) {
                processed.add(key);
                continue;
            }
            Payment saved = paymentRepository.save(Payment.builder()
                    .paymentDate(recurringDate)
                    .amount(amount)
                    .automaticPayment(true)
                    .debt(payment.getDebt())
                    .build());
            processed.add(key);
            Debt fullDebt = debtRepository.findById(debtId)
                    .orElseThrow(() -> new IllegalStateException("debt not found"));
            closeDebtIfPaidOff(saved);
            paymentService.createExpenseTransaction(saved, fullDebt, fullDebt.getUser());
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

    private LocalDate sameDayForMonth(LocalDate original, LocalDate reference) {
        int day = Math.min(original.getDayOfMonth(), reference.lengthOfMonth());
        return LocalDate.of(reference.getYear(), reference.getMonth(), day);
    }
}