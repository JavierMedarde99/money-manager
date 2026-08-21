package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.ports.PaymentService;
import com.money.manager.application.dtos.PaymentRequestDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("")
    public ResponseEntity<PaymentResponseDTO> insertPayment(@RequestBody @Valid PaymentRequestDTO paymentRequestDTO,Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(paymentService.insertPayment(paymentRequestDTO, (User) authentication.getPrincipal()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long id, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(paymentService.getPayment(id, (User) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> updatePayment(@PathVariable Long id, @RequestBody @Valid PaymentRequestDTO paymentRequestDTO, Authentication authentication) throws NotFoundException {
        return ResponseEntity.ok(paymentService.updatePayment(paymentRequestDTO, id, (User) authentication.getPrincipal()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id, Authentication authentication) throws NotFoundException{
        return ResponseEntity.ok(paymentService.deletePayment(id, (User) authentication.getPrincipal()));
    }
}
