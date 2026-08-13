package com.money.manager.application.ports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.money.manager.application.dtos.TransactionFilter;
import com.money.manager.application.dtos.TransactionRequestDTO;
import com.money.manager.application.dtos.TransactionResponseDTO;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user) throws NotFoundException;
    Page<TransactionResponseDTO> getAllTransaction(User user, TransactionFilter transactionFilter, Pageable pageable);
    TransactionResponseDTO getTransaction(Long transactionId) throws NotFoundException;
    TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO,Long transactionId,User user) throws NotFoundException;
    String deleteTransaction(Long transactionId) throws NotFoundException;
}
