package com.money.manager.infrastructure.persistance.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.domain.Transaction;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Type;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.TransactionJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryAdapterTest {

    @Mock
    private PostgresTransactionRepository jpa;

    @Mock
    private PostgresUserRepository jpaUser;

    @Mock
    private PostgresCategoryRepository jpaCategory;

    private TransactionRepositoryAdapter adapter;

    private User user;
    private UserJpa userJpa;

    @BeforeEach
    void setUp() {
        adapter = new TransactionRepositoryAdapter(jpa, jpaUser, jpaCategory);
        user = User.builder().id(1L).username("javi").build();
        userJpa = UserJpa.builder().id(1L).username("javi").build();
    }

    @Test
    void findByUserAndMonth_resolvesUserAndMapsRows() {
        when(jpaUser.findById(1L)).thenReturn(Optional.of(userJpa));
        CategoryJpa categoryJpa = CategoryJpa.builder().id(1L).name("Salary").user(userJpa).build();
        TransactionJpa income = TransactionJpa.builder().id(1L).type(Type.INCOME).price(2500.0)
                .dateTransaction(LocalDate.of(2026, 9, 5)).user(userJpa).category(categoryJpa).build();
        TransactionJpa expense = TransactionJpa.builder().id(2L).type(Type.EXPENSE).price(1800.0)
                .dateTransaction(LocalDate.of(2026, 9, 20)).user(userJpa).category(categoryJpa).build();
        when(jpa.findByUser_IdAndYearAndMonth(userJpa, 2026, 9)).thenReturn(List.of(income, expense));

        List<Transaction> result = adapter.findByUserAndMonth(user, 2026, 9);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(2500.0);
        assertThat(result.get(0).getType()).isEqualTo(Type.INCOME);
        assertThat(result.get(0).getDateTransaction()).isEqualTo(LocalDate.of(2026, 9, 5));
        assertThat(result.get(1).getPrice()).isEqualTo(1800.0);
        assertThat(result.get(1).getType()).isEqualTo(Type.EXPENSE);
        assertThat(result.get(1).getDateTransaction()).isEqualTo(LocalDate.of(2026, 9, 20));
    }

    @Test
    void findByUserAndMonth_throwsWhenUserMissing() {
        when(jpaUser.findById(1L)).thenReturn(Optional.empty());

        try {
            adapter.findByUserAndMonth(user, 2026, 9);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // user not found
        }
    }
}
