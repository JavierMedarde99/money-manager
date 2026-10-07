package com.money.manager.infrastructure.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.application.dtos.SavingsResponseDTO;
import com.money.manager.application.ports.SavingsService;
import com.money.manager.domain.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @GetMapping("")
    public ResponseEntity<List<SavingsResponseDTO>> getSavings(Authentication authentication) {
        return ResponseEntity.ok(savingsService.getSavings((User) authentication.getPrincipal()));
    }
}
