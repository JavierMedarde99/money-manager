package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.application.ports.CategoryService;
import com.money.manager.application.dtos.CategoryRequestDTO;
import com.money.manager.application.dtos.CategoryResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("")
    public ResponseEntity<CategoryResponseDTO> insertCategory(@RequestBody @Valid CategoryRequestDTO categoryDto,
            Authentication authentication) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto, (User) authentication.getPrincipal()));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategoryByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Authentication authentication) {
        Pageable pageable = Pageable.of(page, size, sortBy, SortDirection.getByName(direction));
        return ResponseEntity.ok(categoryService.getCategoryByUser((User) authentication.getPrincipal(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getOneCategory(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.getCategory(id, (User) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
            @RequestBody @Valid CategoryRequestDTO categoryDto, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto, id, (User) authentication.getPrincipal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.deleteCategory(id, (User) authentication.getPrincipal()));
    }

}
