package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.TransactionService;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("transaction")
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("")
    public ResponseEntity<TransactionResponseDTO> postMethodName(@RequestBody TransactionRequestDTO transactionDto,
            Authentication authentication) throws NotFoundException{
        return ResponseEntity
                .ok(transactionService.createTransaction(transactionDto, (User) authentication.getPrincipal()));
    }

}
