package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.DebtService;
import com.money.manager.infrastructure.dtos.DebtRequestDTO;
import com.money.manager.infrastructure.dtos.DebtResponseDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/debt")
@RequiredArgsConstructor
public class DebtController {
    private final DebtService debtService;

    @PostMapping("")
    public ResponseEntity<DebtResponseDTO> insertDebt(@RequestBody DebtRequestDTO debtRequestDTO, Authentication authentication) {
        return ResponseEntity.ok(debtService.insertDebt(debtRequestDTO, (User) authentication.getPrincipal()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DebtResponseDTO>> getDebts(Authentication authentication) {
        return ResponseEntity.ok(debtService.getDebts((User) authentication.getPrincipal()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponseDTO> getDebt(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(debtService.getDebt(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponseDTO> putMethodName(@PathVariable Long id, @RequestBody DebtRequestDTO debtRequestDTO, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(debtService.updateDebt(debtRequestDTO, id, (User) authentication.getPrincipal()));
    }
    
}
