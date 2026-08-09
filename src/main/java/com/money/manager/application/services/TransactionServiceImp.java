package com.money.manager.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.CategoryMapper;
import com.money.manager.application.mappers.TransactionMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.domain.services.TransactionService;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService{

    private final PostgresTransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO,User user) throws NotFoundException {
        Category category = CategoryMapper.fromDto(categoryService.getCategory(transactionRequestDTO.category().id()),user);
        Transaction transaction = TransactionMapper.fromDto(transactionRequestDTO, user,category);
        transactionRepository.save(transaction);
        return TransactionMapper.toDto(transaction);
    }

    @Override
    public List<TransactionResponseDTO> getAllTransaction(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTransaction'");
    }

    @Override
    public TransactionResponseDTO getTransaction(Long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransaction'");
    }

    @Override
    public TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO, Long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransaction'");
    }

    @Override
    public String deleteTransaction(Long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTransaction'");
    }
    
}
