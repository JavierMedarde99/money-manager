package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.infrastructure.dtos.CategoryRequestDTO;
import com.money.manager.infrastructure.dtos.CategoryResponseDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    @PostMapping("")
    public ResponseEntity<CategoryResponseDTO> insertCategory(@RequestBody CategoryRequestDTO categoryDto, Authentication authentication) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto, (User) authentication.getPrincipal()));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponseDTO>> getMethodName(Authentication authentication) {
        return ResponseEntity.ok(categoryService.getCategoryByUser((User) authentication.getPrincipal()));
    }
    
}
