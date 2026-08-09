package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.TransactionService;
import com.money.manager.infrastructure.dtos.TransactionFilter;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;


@RequestMapping("transaction")
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("")
    public ResponseEntity<TransactionResponseDTO> insertTransaction(@RequestBody TransactionRequestDTO transactionDto,
            Authentication authentication) throws NotFoundException {
        return ResponseEntity
                .ok(transactionService.createTransaction(transactionDto, (User) authentication.getPrincipal()));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<TransactionResponseDTO>> obtainListTransaction(@RequestParam(required = false) String type,
            @RequestParam(required = false) String subType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        TransactionFilter filter = new TransactionFilter(
                type != null ? Type.getTypeByName(type) : null,
                subType != null ? Subtype.getSubTypeByName(subType) : null,
                from,
                to);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending());

        return ResponseEntity.ok(
                transactionService.getAllTransaction(
                        (User) authentication.getPrincipal(),
                        filter,
                        pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> obteinTransaction(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequestDTO transactionDto,
            Authentication authentication) throws NotFoundException{
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto, id, (User) authentication.getPrincipal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) throws NotFoundException{
        return ResponseEntity.ok(transactionService.deleteTransaction(id));
    }

}
