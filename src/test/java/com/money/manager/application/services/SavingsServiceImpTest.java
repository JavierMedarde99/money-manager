package com.money.manager.application.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.domain.Savings;
import com.money.manager.domain.SavingsRepository;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Type;

@ExtendWith(MockitoExtension.class)
class SavingsServiceImpTest {

    @Mock
    private SavingsRepository savingsRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private SavingsServiceImp service;

    private User user;
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC);

    @Captor
    private ArgumentCaptor<Savings> savingsCaptor;

    @BeforeEach
    void setUp() {
        service = new SavingsServiceImp(savingsRepository, transactionRepository, clock);
        user = User.builder().id(1L).username("javi").build();
    }

    private Transaction tx(Type type, double price, LocalDate date) {
        return Transaction.builder().id((long) (price + date.getDayOfMonth())).type(type)
                .price(price).dateTransaction(date).user(user).build();
    }

    @Test
    void recalculate_computesIncomeExpenseAndSavings() {
        List<Transaction> txs = List.of(
                tx(Type.INCOME, 2500.0, LocalDate.of(2026, 9, 5)),
                tx(Type.EXPENSE, 1800.0, LocalDate.of(2026, 9, 20)),
                tx(Type.EXPENSE, 200.0, LocalDate.of(2026, 9, 25)));
        when(transactionRepository.findByUserAndMonth(user, 2026, 9)).thenReturn(txs);
        when(savingsRepository.findByUserAndYearAndMonth(user, 2026, 9)).thenReturn(Optional.empty());

        service.recalculate(user, 2026, 9);

        verify(savingsRepository).save(savingsCaptor.capture());
        Savings saved = savingsCaptor.getValue();
        assert saved.getTotalIncome() == 2500.0;
        assert saved.getTotalExpense() == 2000.0;
        assert saved.getSavings() == 500.0;
        assert saved.getUser() == user;
        assert saved.getYear() == 2026;
        assert saved.getMonth() == 9;
        assert saved.getId() == null;
    }

    @Test
    void recalculate_deletesRowWhenMonthHasNoTransactions() {
        Savings existing = Savings.builder().id(10L).user(user).year(2026).month(9)
                .totalIncome(2500.0).totalExpense(2000.0).savings(500.0).build();
        when(transactionRepository.findByUserAndMonth(user, 2026, 9)).thenReturn(List.of());
        when(savingsRepository.findByUserAndYearAndMonth(user, 2026, 9)).thenReturn(Optional.of(existing));

        service.recalculate(user, 2026, 9);

        verify(savingsRepository).delete(existing);
        verify(savingsRepository, never()).save(any());
    }

    @Test
    void recalculate_noopWhenEmptyMonthAndNoRow() {
        when(transactionRepository.findByUserAndMonth(user, 2026, 9)).thenReturn(List.of());
        when(savingsRepository.findByUserAndYearAndMonth(user, 2026, 9)).thenReturn(Optional.empty());

        service.recalculate(user, 2026, 9);

        verify(savingsRepository, never()).save(any());
        verify(savingsRepository, never()).delete(any());
    }

    @Test
    void recalculate_updatesExistingRowInsteadOfInserting() {
        Savings existing = Savings.builder().id(10L).user(user).year(2026).month(9)
                .totalIncome(100.0).totalExpense(50.0).savings(50.0).build();
        List<Transaction> txs = List.of(
                tx(Type.INCOME, 2500.0, LocalDate.of(2026, 9, 5)),
                tx(Type.EXPENSE, 1800.0, LocalDate.of(2026, 9, 20)));
        when(transactionRepository.findByUserAndMonth(user, 2026, 9)).thenReturn(txs);
        when(savingsRepository.findByUserAndYearAndMonth(user, 2026, 9)).thenReturn(Optional.of(existing));

        service.recalculate(user, 2026, 9);

        verify(savingsRepository).save(savingsCaptor.capture());
        Savings saved = savingsCaptor.getValue();
        assert saved.getId() == 10L;
        assert saved.getTotalIncome() == 2500.0;
        assert saved.getTotalExpense() == 1800.0;
        assert saved.getSavings() == 700.0;
    }

    @Test
    void recalculate_twiceProducesSingleRow() {
        Savings existing = Savings.builder().id(10L).user(user).year(2026).month(9)
                .totalIncome(2500.0).totalExpense(1800.0).savings(700.0).build();
        List<Transaction> txs = List.of(tx(Type.INCOME, 2500.0, LocalDate.of(2026, 9, 5)));
        when(transactionRepository.findByUserAndMonth(user, 2026, 9)).thenReturn(txs);
        when(savingsRepository.findByUserAndYearAndMonth(user, 2026, 9))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));

        service.recalculate(user, 2026, 9);
        service.recalculate(user, 2026, 9);

        verify(savingsRepository, org.mockito.Mockito.times(2)).save(savingsCaptor.capture());
        // Both saves carry the same logical row: first insert (no id), then update of id=10.
        Savings first = savingsCaptor.getAllValues().get(0);
        Savings second = savingsCaptor.getAllValues().get(1);
        assert first.getId() == null;
        assert second.getId() == 10L;
        assert first.getYear() == second.getYear() && first.getMonth() == second.getMonth();
    }
}
