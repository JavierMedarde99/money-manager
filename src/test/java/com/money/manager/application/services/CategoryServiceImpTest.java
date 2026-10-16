package com.money.manager.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;
import com.money.manager.domain.Category;
import com.money.manager.domain.CategoryRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
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

    @Test
    void getCategory_ofOwningUser_usesUserScopedLookup() throws NotFoundException {
        when(categoryRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.of(category));

        CategoryResponseDTO result = categoryService.getCategory(5L, user);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Salary");
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void getCategory_ofAnotherUser_throwsNotFound() {
        when(categoryRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(5L, user))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void updateCategory_ofAnotherUser_throwsNotFound() {
        when(categoryRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(
                new CategoryRequestDTO("Salary", "#00FF00"), 5L, user))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).save(any());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void deleteCategory_ofAnotherUser_throwsNotFound() {
        when(categoryRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(5L, user))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository, never()).delete(any());
        verify(categoryRepository, never()).findById(any());
    }
}