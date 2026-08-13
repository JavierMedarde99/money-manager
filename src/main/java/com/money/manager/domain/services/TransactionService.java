package com.money.manager.domain.services;

import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.infrastructure.dtos.TransactionFilter;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user) throws NotFoundException;
    Page<TransactionResponseDTO> getAllTransaction(User user, TransactionFilter transactionFilter, Pageable pageable);
    TransactionResponseDTO getTransaction(Long transactionId) throws NotFoundException;
    TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO,Long transactionId,User user) throws NotFoundException;
    String deleteTransaction(Long transactionId) throws NotFoundException;
}
