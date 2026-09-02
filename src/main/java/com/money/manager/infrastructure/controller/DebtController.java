package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.application.ports.DebtService;
import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
@RequestMapping("/debt")
@RequiredArgsConstructor
public class DebtController {
    private final DebtService debtService;

    @PostMapping("")
    public ResponseEntity<DebtResponseDTO> insertDebt(@RequestBody @Valid DebtRequestDTO debtRequestDTO, Authentication authentication) {
        return ResponseEntity.ok(debtService.insertDebt(debtRequestDTO, (User) authentication.getPrincipal()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DebtResponseDTO>> getDebts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = Pageable.of(page, size, "id", SortDirection.DESC);
        return ResponseEntity.ok(debtService.getDebts((User) authentication.getPrincipal(), pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponseDTO> getDebt(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(debtService.getDebt(id, (User) authentication.getPrincipal()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponseDTO> updateDebt(@PathVariable Long id, @RequestBody @Valid DebtRequestDTO debtRequestDTO, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(debtService.updateDebt(debtRequestDTO, id, (User) authentication.getPrincipal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDebt(@PathVariable Long id, Authentication authentication) throws NotFoundException{
        return ResponseEntity.ok(debtService.deleteDebt(id, (User) authentication.getPrincipal()));
    }
    
}
