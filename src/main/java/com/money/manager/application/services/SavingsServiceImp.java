package com.money.manager.application.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.money.manager.application.ports.SavingsService;
import com.money.manager.domain.Savings;
import com.money.manager.domain.SavingsRepository;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Type;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsServiceImp implements SavingsService {

    private final SavingsRepository savingsRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    @Override
    public void recalculate(User user, int year, int month) {
        List<Transaction> transactions = transactionRepository.findByUserAndMonth(user, year, month);
        Optional<Savings> existing = savingsRepository.findByUserAndYearAndMonth(user, year, month);

        if (transactions.isEmpty()) {
            existing.ifPresent(savingsRepository::delete);
            return;
        }

        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == Type.INCOME)
                .mapToDouble(t -> t.getPrice() == null ? 0.0 : t.getPrice())
                .sum();
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == Type.EXPENSE)
                .mapToDouble(t -> t.getPrice() == null ? 0.0 : t.getPrice())
                .sum();

        Savings savings = existing.orElseGet(() -> Savings.builder().user(user).year(year).month(month).build());
        savings.setTotalIncome(totalIncome);
        savings.setTotalExpense(totalExpense);
        savings.setSavings(totalIncome - totalExpense);
        savingsRepository.save(savings);
    }

    @Override
    public void recalculateAll() {
        // Implemented in Task 7 (scheduler retrospective pass).
        throw new UnsupportedOperationException("not implemented");
    }
}
