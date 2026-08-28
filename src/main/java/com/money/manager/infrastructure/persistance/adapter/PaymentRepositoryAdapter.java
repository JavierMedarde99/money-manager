package com.money.manager.infrastructure.persistance.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.infrastructure.persistance.PostgresDebtRepository;
import com.money.manager.infrastructure.persistance.PostgresPaymentRepository;
import com.money.manager.infrastructure.persistance.entity.DebtJpa;
import com.money.manager.infrastructure.persistance.mapper.PaymentJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PostgresPaymentRepository jpa;
    private final PostgresDebtRepository jpaDebt;

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return jpa.findById(id).map(PaymentJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdAndDebt_User_Id(Long id, Long userId) {
        return jpa.findByIdAndDebt_User_Id(id, userId).map(PaymentJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAutomaticPaymentsForOpenDebts() {
        return jpa.findByAutomaticPaymentTrueAndDebt_EndDateIsNull().stream()
                .map(PaymentJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDebtAmountAndMonth(com.money.manager.domain.Debt debt, Double amount, int year, int month) {
        DebtJpa debtJpa = jpaDebt.findById(debt.getId())
                .orElseThrow(() -> new IllegalStateException("debt not found"));
        return jpa.existsByDebtAmountAndMonth(debtJpa, amount, year, month);
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        DebtJpa debtJpa = jpaDebt.findById(payment.getDebt().getId())
                .orElseThrow(() -> new IllegalStateException("debt not found"));
        return PaymentJpaMapper.toDomain(jpa.save(PaymentJpaMapper.toJpa(payment, debtJpa)));
    }

    @Override
    @Transactional
    public void delete(Payment payment) {
        jpa.deleteById(payment.getId());
    }
}
