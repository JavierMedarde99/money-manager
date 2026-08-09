package com.money.manager.domain.services;

import java.util.List;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user) throws NotFoundException;
    List<TransactionResponseDTO> getAllTransaction(User user);
    TransactionResponseDTO getTransaction(Long transactionId);
    TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO,Long transactionId);
    String deleteTransaction(Long transactionId);
}
