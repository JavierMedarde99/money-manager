package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.money.manager.application.dtos.TransactionFilter;
import com.money.manager.application.dtos.TransactionRequestDTO;
import com.money.manager.application.dtos.TransactionResponseDTO;
import com.money.manager.application.ports.CategoryService;
import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.SortDirection;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImpTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionServiceImp transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<org.springframework.data.domain.Pageable> springPageableCaptor;

    private User user;
    private Category category;
    private Transaction transaction;
    private TransactionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("javi").build();
        category = Category.builder().id(5L).name("Salary").color("#00FF00").user(user).build();
        transaction = Transaction.builder()
                .id(10L)
                .name("Paycheck")
                .dateTransaction(LocalDate.of(2026, 1, 15))
                .amount(1)
                .price(1500.0)
                .type(Type.INCOME)
                .subtype(Subtype.FIXED)
                .user(user)
                .category(category)
                .build();
        requestDTO = new TransactionRequestDTO(
                "Paycheck", "2026-01-15", 1, 1500.0,
                "income", "fixed", new com.money.manager.application.dtos.CategoryResponseDTO(5L, "Salary", "#00FF00"));
    }

    @Test
    void createTransaction_mapsRequestSavesAndReturnsDto() throws NotFoundException {
        when(categoryService.findCategory(5L, user)).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDTO result = transactionService.createTransaction(requestDTO, user);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Paycheck");
        assertThat(saved.getDateTransaction()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(saved.getAmount()).isEqualTo(1);
        assertThat(saved.getPrice()).isEqualTo(1500.0);
        assertThat(saved.getType()).isEqualTo(Type.INCOME);
        assertThat(saved.getSubtype()).isEqualTo(Subtype.FIXED);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCategory()).isSameAs(category);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Paycheck");
        assertThat(result.transactionDate()).isEqualTo("2026-01-15");
        assertThat(result.amount()).isEqualTo(1);
        assertThat(result.price()).isEqualTo(1500.0);
        assertThat(result.transactionType()).isEqualTo("income");
        assertThat(result.transactionSubtype()).isEqualTo("fixed");
        assertThat(result.category().id()).isEqualTo(5L);
    }

    @Test
    void createTransaction_withUnknownCategory_propagatesNotFoundAndDoesNotSave() throws NotFoundException {
        when(categoryService.findCategory(5L, user))
                .thenThrow(new NotFoundException("category not found"));

        assertThatThrownBy(() -> transactionService.createTransaction(requestDTO, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("category not found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getAllTransaction_mapsContentAndUsesAscendingSort() {
        com.money.manager.domain.paging.Pageable pageable = com.money.manager.domain.paging.Pageable.of(0, 10, "id", SortDirection.ASC);
        when(transactionRepository.findByFilters(eq(user), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        List.of(transaction), PageRequest.of(0, 10, Sort.by("id").ascending()), 1));

        Page<TransactionResponseDTO> result = transactionService.getAllTransaction(user, new TransactionFilter(null, null, null, null), pageable);

        verify(transactionRepository).findByFilters(eq(user), isNull(), isNull(), isNull(), isNull(),
                springPageableCaptor.capture());
        org.springframework.data.domain.Pageable used = springPageableCaptor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(10L);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void getAllTransaction_withDescDirection_usesDescendingSort() {
        com.money.manager.domain.paging.Pageable pageable = com.money.manager.domain.paging.Pageable.of(2, 5, "id", SortDirection.DESC);
        when(transactionRepository.findByFilters(eq(user), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        transactionService.getAllTransaction(user, new TransactionFilter(null, null, null, null), pageable);

        verify(transactionRepository).findByFilters(eq(user), isNull(), isNull(), isNull(), isNull(),
                springPageableCaptor.capture());
        org.springframework.data.domain.Pageable used = springPageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(2);
        assertThat(used.getPageSize()).isEqualTo(5);
        assertThat(used.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getAllTransaction_forwardsAllFilterValues() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        com.money.manager.domain.paging.Pageable pageable = com.money.manager.domain.paging.Pageable.of(0, 10, "id", SortDirection.ASC);
        when(transactionRepository.findByFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        transactionService.getAllTransaction(user, new TransactionFilter(Type.EXPENSE, Subtype.VARIABLE, from, to), pageable);

        verify(transactionRepository).findByFilters(eq(user), eq(Type.EXPENSE), eq(Subtype.VARIABLE),
                eq(from), eq(to), springPageableCaptor.capture());
        assertThat(springPageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getTransaction_withExistingId_returnsDto() throws NotFoundException {
        when(transactionRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(transaction));

        TransactionResponseDTO result = transactionService.getTransaction(10L, user);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Paycheck");
    }

    @Test
    void getTransaction_withUnknownId_throwsNotFound() {
        when(transactionRepository.findByIdAndUser_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(99L, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("transaction not found");
    }

    @Test
    void updateTransaction_updatesOwnedTransactionFields() throws NotFoundException {
        Category newCategory = Category.builder().id(7L).name("Food").color("#FF0000").user(user).build();
        TransactionRequestDTO updateDTO = new TransactionRequestDTO(
                "Groceries", "2026-02-20", 3, 45.5,
                "expense", "variable", new com.money.manager.application.dtos.CategoryResponseDTO(7L, "Food", "#FF0000"));
        when(transactionRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(transaction));
        when(categoryService.findCategory(7L, user)).thenReturn(newCategory);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDTO result = transactionService.updateTransaction(updateDTO, 10L, user);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Groceries");
        assertThat(saved.getDateTransaction()).isEqualTo(LocalDate.of(2026, 2, 20));
        assertThat(saved.getAmount()).isEqualTo(3);
        assertThat(saved.getPrice()).isEqualTo(45.5);
        assertThat(saved.getType()).isEqualTo(Type.EXPENSE);
        assertThat(saved.getSubtype()).isEqualTo(Subtype.VARIABLE);
        assertThat(saved.getCategory()).isSameAs(newCategory);

        assertThat(result.name()).isEqualTo("Groceries");
        assertThat(result.transactionType()).isEqualTo("expense");
        assertThat(result.transactionSubtype()).isEqualTo("variable");
    }

    @Test
    void updateTransaction_ofAnotherUser_throwsNotFound() {
        when(transactionRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(requestDTO, 10L, user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("transaction not found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deleteTransaction_deletesOwnedTransactionAndReturnsMessage() throws NotFoundException {
        when(transactionRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(transaction));

        String result = transactionService.deleteTransaction(10L, user);

        verify(transactionRepository).delete(transaction);
        assertThat(result).isEqualTo("transaction delete");
    }

    @Test
    void deleteTransaction_ofAnotherUser_throwsNotFound() {
        when(transactionRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(10L, user))
                .isInstanceOf(NotFoundException.class);

        verify(transactionRepository, never()).delete(any());
    }
}
