package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;

@ExtendWith(MockitoExtension.class)
class DebtServiceImpTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private DebtServiceImp debtService;

    private User user;
    private Debt debt;

    @BeforeEach
    void setUp() {
        debtService = new DebtServiceImp(debtRepository, paymentRepository);
        user = User.builder().id(1L).username("javi").build();
        debt = Debt.builder()
                .id(10L)
                .name("Car")
                .totalAmount(3000.0)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(null)
                .user(user)
                .build();
    }

    @Test
    void getDebts_paginatesEachDebtsPayments() {
        Payment payment = Payment.builder()
                .id(50L)
                .paymentDate(LocalDate.of(2026, 1, 15))
                .amount(500.0)
                .automaticPayment(true)
                .debt(debt)
                .build();
        org.springframework.data.domain.PageImpl<Payment> springPage = new org.springframework.data.domain.PageImpl<>(
                List.of(payment),
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by("id").descending()),
                1);

        when(debtRepository.findByUser(user)).thenReturn(List.of(debt));
        when(paymentRepository.findByDebt_Id(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any(
                org.springframework.data.domain.Pageable.class))).thenReturn(springPage);

        Pageable pageable = Pageable.of(0, 10, "id", SortDirection.DESC);
        List<DebtResponseDTO> result = debtService.getDebts(user, pageable);

        assertThat(result).hasSize(1);
        DebtResponseDTO dto = result.get(0);
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.payments().content()).hasSize(1);
        assertThat(dto.payments().content().get(0).id()).isEqualTo(50L);
        assertThat(dto.payments().content().get(0).paymentDate()).isEqualTo("2026-01-15");
        assertThat(dto.payments().content().get(0).amount()).isEqualTo(500.0);
        assertThat(dto.payments().totalElements()).isEqualTo(1L);
    }

    @Test
    void getDebts_usesDescendingPageablePerDebt() {
        when(debtRepository.findByUser(user)).thenReturn(List.of(debt));
        when(paymentRepository.findByDebt_Id(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0));

        debtService.getDebts(user, Pageable.of(0, 10, "id", SortDirection.DESC));

        verify(paymentRepository).findByDebt_Id(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.argThat(p ->
                        p.getSort().getOrderFor("id").getDirection()
                                == org.springframework.data.domain.Sort.Direction.DESC));
    }

    @Test
    void getDebt_returnsPaymentsAsSinglePage() throws NotFoundException {
        Payment payment = Payment.builder()
                .id(50L)
                .paymentDate(LocalDate.of(2026, 1, 15))
                .amount(500.0)
                .automaticPayment(true)
                .debt(debt)
                .build();
        debt.setPayments(new java.util.HashSet<>(List.of(payment)));

        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(debt));

        DebtResponseDTO result = debtService.getDebt(10L, user);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.payments().content()).hasSize(1);
        assertThat(result.payments().content().get(0).id()).isEqualTo(50L);
    }

    @Test
    void getDebt_ofAnotherUser_throwsNotFound() {
        when(debtRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> debtService.getDebt(10L, user))
                .isInstanceOf(NotFoundException.class);

        verify(paymentRepository, never()).findByDebt_Id(any(), any());
    }

    @Test
    void insertDebt_returnsEmptyPaymentsPage() {
        DebtRequestDTO request = new DebtRequestDTO("Car", 3000.0, "2026-01-01", null);
        when(debtRepository.save(any(Debt.class))).thenReturn(debt);

        DebtResponseDTO result = debtService.insertDebt(request, user);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.payments().content()).isEmpty();
        assertThat(result.payments().totalElements()).isZero();
    }
}