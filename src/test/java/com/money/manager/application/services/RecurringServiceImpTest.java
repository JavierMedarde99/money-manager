package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.application.ports.DebtService;
import com.money.manager.domain.Category;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

@ExtendWith(MockitoExtension.class)
class RecurringServiceImpTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtService debtService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private RecurringServiceImp recurringService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-01T00:05:00Z"), ZoneOffset.UTC);
        recurringService = new RecurringServiceImp(transactionRepository, paymentRepository, debtRepository,
                debtService, clock);
        user = User.builder().id(1L).username("javi").build();
        category = Category.builder().id(5L).name("Salary").color("#00FF00").user(user).build();
    }

    @Test
    void processFixedTransactions_insertsCopyWithSameDayOfMonth() {
        Transaction fixed = Transaction.builder().id(10L).name("Paycheck")
                .dateTransaction(LocalDate.of(2026, 1, 15)).amount(1).price(1500.0)
                .type(Type.INCOME).subtype(Subtype.FIXED).user(user).category(category).build();
        when(transactionRepository.findBySubtype(Subtype.FIXED)).thenReturn(List.of(fixed));
        when(transactionRepository.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                user, category, "Paycheck", 1, Type.INCOME, Subtype.FIXED, 2026, 2)).thenReturn(false);

        recurringService.processFixedTransactions();

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getDateTransaction()).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(saved.getName()).isEqualTo("Paycheck");
        assertThat(saved.getAmount()).isEqualTo(1);
        assertThat(saved.getPrice()).isEqualTo(1500.0);
        assertThat(saved.getType()).isEqualTo(Type.INCOME);
        assertThat(saved.getSubtype()).isEqualTo(Subtype.FIXED);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCategory()).isSameAs(category);
    }

    @Test
    void processFixedTransactions_skipsWhenTransactionExistsForMonth() {
        Transaction fixed = Transaction.builder().id(10L).name("Paycheck")
                .dateTransaction(LocalDate.of(2026, 1, 15)).amount(1).price(1500.0)
                .type(Type.INCOME).subtype(Subtype.FIXED).user(user).category(category).build();
        when(transactionRepository.findBySubtype(Subtype.FIXED)).thenReturn(List.of(fixed));
        when(transactionRepository.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                user, category, "Paycheck", 1, Type.INCOME, Subtype.FIXED, 2026, 2)).thenReturn(true);

        recurringService.processFixedTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processFixedTransactions_clampsDayWhenMonthHasNoSuchDay() {
        Transaction fixed = Transaction.builder().id(10L).name("Rent")
                .dateTransaction(LocalDate.of(2026, 1, 31)).amount(1).price(800.0)
                .type(Type.EXPENSE).subtype(Subtype.FIXED).user(user).category(category).build();
        when(transactionRepository.findBySubtype(Subtype.FIXED)).thenReturn(List.of(fixed));
        when(transactionRepository.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                user, category, "Rent", 1, Type.EXPENSE, Subtype.FIXED, 2026, 2)).thenReturn(false);

        recurringService.processFixedTransactions();

        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getDateTransaction()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void processFixedTransactions_savesNothingWhenNoFixedTransactionsExist() {
        when(transactionRepository.findBySubtype(Subtype.FIXED)).thenReturn(List.of());

        recurringService.processFixedTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processAutomaticPayments_insertsCopyWithSameDayOfMonth() {
        Debt debt = Debt.builder().id(1L).name("Car loan").totalAmount(1000.0).user(user)
                .payments(Set.of()).build();
        Payment auto = Payment.builder().id(20L).paymentDate(LocalDate.of(2026, 1, 15))
                .amount(100.0).automaticPayment(true).debt(debt).build();
        when(paymentRepository.findAutomaticPaymentsForOpenDebts()).thenReturn(List.of(auto));
        when(paymentRepository.existsByDebtAmountAndMonth(debt, 100.0, 2026, 2)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));

        recurringService.processAutomaticPayments();

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getPaymentDate()).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(saved.getAmount()).isEqualTo(100.0);
        assertThat(saved.getAutomaticPayment()).isTrue();
        assertThat(saved.getDebt()).isSameAs(debt);
        verify(debtService, never()).closeDebt(any(Debt.class));
    }

    @Test
    void processAutomaticPayments_skipsWhenPaymentExistsForMonth() {
        Debt debt = Debt.builder().id(1L).name("Car loan").totalAmount(1000.0).user(user).build();
        Payment auto = Payment.builder().id(20L).paymentDate(LocalDate.of(2026, 1, 15))
                .amount(100.0).automaticPayment(true).debt(debt).build();
        when(paymentRepository.findAutomaticPaymentsForOpenDebts()).thenReturn(List.of(auto));
        when(paymentRepository.existsByDebtAmountAndMonth(debt, 100.0, 2026, 2)).thenReturn(true);

        recurringService.processAutomaticPayments();

        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(debtRepository, debtService);
    }

    @Test
    void processAutomaticPayments_closesDebtWhenTotalPaidReachesTotalAmount() {
        Payment existing = Payment.builder().id(1L).paymentDate(LocalDate.of(2026, 1, 5))
                .amount(60.0).automaticPayment(false).build();
        Debt debt = Debt.builder().id(1L).name("Car loan").totalAmount(100.0).user(user)
                .payments(Set.of(existing)).build();
        Payment auto = Payment.builder().id(20L).paymentDate(LocalDate.of(2026, 1, 15))
                .amount(40.0).automaticPayment(true).debt(debt).build();
        when(paymentRepository.findAutomaticPaymentsForOpenDebts()).thenReturn(List.of(auto));
        when(paymentRepository.existsByDebtAmountAndMonth(debt, 40.0, 2026, 2)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Debt debtAfterInsert = Debt.builder().id(1L).name("Car loan").totalAmount(100.0).user(user)
                .payments(Set.of(existing, auto)).build();
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debtAfterInsert));

        recurringService.processAutomaticPayments();

        verify(debtService).closeDebt(debtAfterInsert);
    }

    @Test
    void processAutomaticPayments_savesNothingWhenNoAutomaticPaymentsExist() {
        when(paymentRepository.findAutomaticPaymentsForOpenDebts()).thenReturn(List.of());

        recurringService.processAutomaticPayments();

        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(debtRepository, debtService);
    }
}