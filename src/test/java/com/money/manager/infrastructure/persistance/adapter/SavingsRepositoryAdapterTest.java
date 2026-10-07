package com.money.manager.infrastructure.persistance.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.money.manager.domain.Savings;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.PostgresSavingsRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.SavingsJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

@ExtendWith(MockitoExtension.class)
class SavingsRepositoryAdapterTest {

    @Mock
    private PostgresSavingsRepository jpa;

    @Mock
    private PostgresUserRepository jpaUser;

    private SavingsRepositoryAdapter adapter;

    private User user;
    private UserJpa userJpa;

    @BeforeEach
    void setUp() {
        adapter = new SavingsRepositoryAdapter(jpa, jpaUser);
        user = User.builder().id(1L).username("javi").build();
        userJpa = UserJpa.builder().id(1L).username("javi").build();
    }

    @Test
    void findByUser_queriesByUserIdAndMapsRows() {
        SavingsJpa row = SavingsJpa.builder().id(10L).year(2026).month(9)
                .totalIncome(2500.0).totalExpense(1800.0).savings(700.0).user(userJpa).build();
        when(jpa.findByUser_IdOrderByYearAscMonthAsc(1L)).thenReturn(List.of(row));

        List<Savings> result = adapter.findByUser(user);

        assertThat(result).hasSize(1);
        Savings savings = result.get(0);
        assertThat(savings.getId()).isEqualTo(10L);
        assertThat(savings.getYear()).isEqualTo(2026);
        assertThat(savings.getMonth()).isEqualTo(9);
        assertThat(savings.getTotalIncome()).isEqualTo(2500.0);
        assertThat(savings.getTotalExpense()).isEqualTo(1800.0);
        assertThat(savings.getSavings()).isEqualTo(700.0);
        assertThat(savings.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void findByUserAndYearAndMonth_resolvesUserThenQueries() {
        when(jpaUser.findById(1L)).thenReturn(Optional.of(userJpa));
        SavingsJpa row = SavingsJpa.builder().id(11L).year(2026).month(9)
                .totalIncome(2500.0).totalExpense(1800.0).savings(700.0).user(userJpa).build();
        when(jpa.findByUser_IdAndYearAndMonth(1L, 2026, 9)).thenReturn(Optional.of(row));

        Optional<Savings> result = adapter.findByUserAndYearAndMonth(user, 2026, 9);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(11L);
    }

    @Test
    void findByUserAndYearAndMonth_emptyWhenUserMissing() {
        when(jpaUser.findById(1L)).thenReturn(Optional.empty());

        assertThat(adapter.findByUserAndYearAndMonth(user, 2026, 9)).isEmpty();
    }

    @Test
    void save_resolvesUserJpaThenSaves() {
        when(jpaUser.findById(1L)).thenReturn(Optional.of(userJpa));
        when(jpa.save(any(SavingsJpa.class))).thenAnswer(inv -> {
            SavingsJpa jpaArg = inv.getArgument(0);
            return SavingsJpa.builder().id(12L).year(jpaArg.getYear()).month(jpaArg.getMonth())
                    .totalIncome(jpaArg.getTotalIncome()).totalExpense(jpaArg.getTotalExpense())
                    .savings(jpaArg.getSavings()).user(jpaArg.getUser()).build();
        });

        Savings saved = adapter.save(Savings.builder().user(user).year(2026).month(10)
                .totalIncome(2500.0).totalExpense(1500.0).savings(1000.0).build());

        assertThat(saved.getId()).isEqualTo(12L);
        assertThat(saved.getYear()).isEqualTo(2026);
        assertThat(saved.getMonth()).isEqualTo(10);
    }

    @Test
    void deleteByUser_Id_delegatesToJpa() {
        adapter.deleteByUser_Id(1L);

        verify(jpa).deleteByUser_Id(1L);
    }
}
