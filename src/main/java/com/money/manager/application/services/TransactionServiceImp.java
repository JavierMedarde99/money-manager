package com.money.manager.application.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.CategoryMapper;
import com.money.manager.application.mappers.TransactionMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.domain.services.TransactionService;
import com.money.manager.infrastructure.dtos.TransactionFilter;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {

    private final PostgresTransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user)
            throws NotFoundException {
        Category category = CategoryMapper.fromDto(categoryService.getCategory(transactionRequestDTO.category().id()),
                user);
        Transaction transaction = TransactionMapper.fromDto(transactionRequestDTO, user, category);
        transactionRepository.save(transaction);
        return TransactionMapper.toDto(transaction);
    }

    @Override
    public Page<TransactionResponseDTO> getAllTransaction(
            User user,
            TransactionFilter filter,
            Pageable pageable) {

        Page<Transaction> transactions = transactionRepository.findByFilters(
                user,
                filter.type(),
                filter.subtype(),
                filter.from(),
                filter.to(),
                pageable);

        return transactions.map(TransactionMapper::toDto);
    }

    @Override
    public TransactionResponseDTO getTransaction(Long transactionId) throws NotFoundException {
        Optional<Transaction> optTransaction = transactionRepository.findById(transactionId);
        Transaction transaction = optTransaction.orElseThrow(() -> new NotFoundException("transaction not found"));
        return TransactionMapper.toDto(transaction);
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
