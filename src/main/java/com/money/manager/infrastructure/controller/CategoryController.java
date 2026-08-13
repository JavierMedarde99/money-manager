package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@CrossOrigin(origins =  "*")
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("")
    public ResponseEntity<CategoryResponseDTO> insertCategory(@RequestBody CategoryRequestDTO categoryDto,
            Authentication authentication) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto, (User) authentication.getPrincipal()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategoryByUser(Authentication authentication) {
        return ResponseEntity.ok(categoryService.getCategoryByUser((User) authentication.getPrincipal()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getOneCategory(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.getCategory(id, (User) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
            @RequestBody CategoryRequestDTO categoryDto, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto, id, (User) authentication.getPrincipal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(categoryService.deleteCartegory(id, (User) authentication.getPrincipal()));
    }

}
