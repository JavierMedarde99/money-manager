package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.money.manager.application.dtos.CategoryResponseDTO;
import com.money.manager.domain.Category;
import com.money.manager.domain.CategoryRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImpTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImp categoryService;

    @Captor
    private ArgumentCaptor<org.springframework.data.domain.Pageable> springPageableCaptor;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("javi").build();
        category = Category.builder().id(5L).name("Salary").color("#00FF00").user(user).build();
    }

    @Test
    void getCategoryByUser_mapsContentAndUsesDefaultIdSort() {
        Pageable pageable = Pageable.of(0, 10, "id", SortDirection.ASC);
        when(categoryRepository.findByUser(eq(user), any()))
                .thenReturn(new PageImpl<>(List.of(category), PageRequest.of(0, 10, Sort.by("id").ascending()), 1));

        Page<CategoryResponseDTO> result = categoryService.getCategoryByUser(user, pageable);

        verify(categoryRepository).findByUser(eq(user), springPageableCaptor.capture());
        org.springframework.data.domain.Pageable used = springPageableCaptor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(5L);
        assertThat(result.content().get(0).name()).isEqualTo("Salary");
        assertThat(result.content().get(0).color()).isEqualTo("#00FF00");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void getCategoryByUser_withDescDirection_usesDescendingSort() {
        Pageable pageable = Pageable.of(1, 5, "name", SortDirection.DESC);
        when(categoryRepository.findByUser(eq(user), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 5), 0));

        categoryService.getCategoryByUser(user, pageable);

        verify(categoryRepository).findByUser(eq(user), springPageableCaptor.capture());
        org.springframework.data.domain.Pageable used = springPageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(1);
        assertThat(used.getPageSize()).isEqualTo(5);
        assertThat(used.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getCategoryByUser_sortByColor_usesColorProperty() {
        Pageable pageable = Pageable.of(0, 10, "color", SortDirection.ASC);
        when(categoryRepository.findByUser(eq(user), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10, Sort.by("color").ascending()), 0));

        categoryService.getCategoryByUser(user, pageable);

        verify(categoryRepository).findByUser(eq(user), springPageableCaptor.capture());
        assertThat(springPageableCaptor.getValue().getSort().getOrderFor("color").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getCategoryByUser_unknownSortBy_fallsBackToId() {
        Pageable pageable = Pageable.of(0, 10, "DROP TABLE categories", SortDirection.ASC);
        when(categoryRepository.findByUser(eq(user), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        categoryService.getCategoryByUser(user, pageable);

        verify(categoryRepository).findByUser(eq(user), springPageableCaptor.capture());
        assertThat(springPageableCaptor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }
}