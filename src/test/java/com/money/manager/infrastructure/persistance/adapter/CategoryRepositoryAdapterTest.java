package com.money.manager.infrastructure.persistance.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.money.manager.domain.Category;
import com.money.manager.domain.User;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock
    private PostgresCategoryRepository jpa;

    @Mock
    private PostgresUserRepository jpaUser;

    private CategoryRepositoryAdapter adapter;

    private User user;

    @BeforeEach
    void setUp() {
        adapter = new CategoryRepositoryAdapter(jpa, jpaUser);
        user = User.builder().id(1L).username("javi").build();
    }

    @Test
    void findByUser_withPageable_queriesByUserIdAndMapsRows() {
        UserJpa userJpa = UserJpa.builder().id(1L).username("javi").build();
        CategoryJpa salary = CategoryJpa.builder().id(1L).name("Salary").color("#00FF00").user(userJpa).build();
        CategoryJpa food = CategoryJpa.builder().id(2L).name("Food").color("#FF0000").user(userJpa).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(jpa.findByUser_Id(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(salary, food), pageable, 2));

        Page<Category> result = adapter.findByUser(user, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Salary");
        assertThat(result.getContent().get(0).getColor()).isEqualTo("#00FF00");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByUser_withPageable_returnsEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(jpa.findByUser_Id(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<Category> result = adapter.findByUser(user, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}