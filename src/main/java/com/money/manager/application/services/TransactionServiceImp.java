package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.CategoryMapper;
import com.money.manager.application.mappers.TransactionMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.CategoryService;
import com.money.manager.domain.services.TransactionService;
import com.money.manager.infrastructure.dtos.TransactionFilter;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user)
            throws NotFoundException {
        Category category = CategoryMapper.fromDto(categoryService.getCategory(transactionRequestDTO.category().id(),
                user),
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
    public TransactionResponseDTO getTransaction(Long transactionId, User user) throws NotFoundException {
        Transaction transaction = findById(transactionId, user);
        return TransactionMapper.toDto(transaction);
    }

    @Override
    public TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO, Long transactionId,User user) throws NotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Transaction transaction = findById(transactionId, user);
        transaction.setName(transactionRequestDTO.name());
        transaction.setDateTransaction( LocalDate.parse(transactionRequestDTO.transactionDate(), formatter));
        transaction.setPrices(transactionRequestDTO.price());
        transaction.setAmount(transactionRequestDTO.amount());
        transaction.setSubtype(Subtype.getSubTypeByName(transactionRequestDTO.transactionSubtype()));
        transaction.setType(Type.getTypeByName(transactionRequestDTO.transactionType()));
        transaction.setCategory(CategoryMapper.fromDto(categoryService.getCategory(transactionRequestDTO.category().id(),
                user),
                user));
        transactionRepository.save(transaction);
        return TransactionMapper.toDto(transaction);
    }

    @Override
    public String deleteTransaction(Long transactionId, User user) throws NotFoundException{
        Transaction transaction = findById(transactionId, user);
        transactionRepository.delete(transaction);
        return "transaction delete"; 
    }

    private Transaction findById(Long id, User user) throws NotFoundException{
        Optional<Transaction> optTransaction = transactionRepository.findByIdAndUser_Id(id, user.getId());
        return optTransaction.orElseThrow(() -> new NotFoundException("transaction not found"));
    }
}
