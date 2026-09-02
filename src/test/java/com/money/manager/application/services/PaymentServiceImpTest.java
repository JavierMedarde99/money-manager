package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.application.dtos.DebtDTO;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.ports.DebtService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImpTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private DebtService debtService;

    @Mock
    private Clock clock;

    private PaymentServiceImp paymentService;

    private User user;
    private Debt debt;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImp(paymentRepository, debtRepository, debtService, clock);
        user = User.builder().id(1L).username("javi").build();
        debt = Debt.builder().id(10L).totalAmount(3000.0).user(user)
                .payments(new java.util.HashSet<>()).build();
        savedPayment = Payment.builder()
                .id(50L)
                .paymentDate(LocalDate.of(2026, 1, 15))
                .amount(500.0)
                .automaticPayment(true)
                .debt(debt)
                .build();
    }

    @Test
    void insertPayment_fixedPastDate_createsBackfillUpToCurrentMonth() throws NotFoundException {
        when(clock.instant()).thenReturn(Instant.parse("2026-04-15T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        PaymentRequestDTO dto = new PaymentRequestDTO(
                "2026-01-15", 500.0, true, new DebtDTO(10L, "Car", 3000.0, null, null));
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return Payment.builder().id(100L).paymentDate(p.getPaymentDate()).amount(p.getAmount())
                    .automaticPayment(p.getAutomaticPayment()).debt(p.getDebt()).build();
        });
        when(debtRepository.findById(10L)).thenReturn(Optional.of(debt));
        when(paymentRepository.existsByDebtAmountAndMonth(any(), any(), anyInt(), anyInt())).thenReturn(false);

        PaymentResponseDTO result = paymentService.insertPayment(dto, user);

        assertThat(result.paymentDate()).isEqualTo("2026-01-15");
        verify(paymentRepository, times(4)).save(any(Payment.class));
    }

    @Test
    void insertPayment_fixedCurrentMonth_savesOnlyOnce() throws NotFoundException {
        when(clock.instant()).thenReturn(Instant.parse("2026-01-15T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        PaymentRequestDTO dto = new PaymentRequestDTO(
                "2026-01-15", 500.0, true, new DebtDTO(10L, "Car", 3000.0, null, null));
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        paymentService.insertPayment(dto, user);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void insertPayment_nonAutomatic_savesOnlyOnce() throws NotFoundException {
        PaymentRequestDTO dto = new PaymentRequestDTO(
                "2026-01-15", 500.0, false, new DebtDTO(10L, "Car", 3000.0, null, null));
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return Payment.builder().id(200L).paymentDate(p.getPaymentDate()).amount(p.getAmount())
                    .automaticPayment(p.getAutomaticPayment()).debt(p.getDebt()).build();
        });

        paymentService.insertPayment(dto, user);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentRepository, never()).existsByDebtAmountAndMonth(any(), any(), anyInt(), anyInt());
    }

    @Test
    void insertPayment_fixedPastDate_skipsExistingMonths() throws NotFoundException {
        when(clock.instant()).thenReturn(Instant.parse("2026-04-15T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        PaymentRequestDTO dto = new PaymentRequestDTO(
                "2026-01-15", 500.0, true, new DebtDTO(10L, "Car", 3000.0, null, null));
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return Payment.builder().id(200L).paymentDate(p.getPaymentDate()).amount(p.getAmount())
                    .automaticPayment(p.getAutomaticPayment()).debt(p.getDebt()).build();
        });
        when(debtRepository.findById(10L)).thenReturn(Optional.of(debt));
        when(paymentRepository.existsByDebtAmountAndMonth(any(), any(), anyInt(), anyInt()))
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        paymentService.insertPayment(dto, user);

        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    void insertPayment_fixed_callsCloseDebtCheckForEachBackfilledPayment() throws NotFoundException {
        when(clock.instant()).thenReturn(Instant.parse("2026-03-15T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        debt = Debt.builder().id(10L).totalAmount(1000.0).user(user)
                .payments(new java.util.HashSet<>()).build();
        PaymentRequestDTO dto = new PaymentRequestDTO(
                "2026-01-15", 500.0, true, new DebtDTO(10L, "Car", 1000.0, null, null));
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return Payment.builder().id(300L).paymentDate(p.getPaymentDate()).amount(p.getAmount())
                    .automaticPayment(p.getAutomaticPayment()).debt(p.getDebt()).build();
        });
        when(debtRepository.findById(10L)).thenAnswer(inv -> {
            debt.getPayments().add(Payment.builder().amount(500.0).build());
            return Optional.of(debt);
        });
        when(paymentRepository.existsByDebtAmountAndMonth(any(), any(), anyInt(), anyInt())).thenReturn(false);

        paymentService.insertPayment(dto, user);

        verify(paymentRepository, times(3)).save(any(Payment.class));
        verify(debtRepository, times(2)).findById(10L);
        verify(debtService, times(1)).closeDebt(any(Debt.class));
    }
}