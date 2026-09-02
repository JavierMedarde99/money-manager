package com.money.manager.application.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.application.mappers.TransactionMapper;
import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.application.ports.CategoryService;
import com.money.manager.application.ports.TransactionService;
import com.money.manager.application.dtos.TransactionFilter;
import com.money.manager.application.dtos.TransactionRequestDTO;
import com.money.manager.application.dtos.TransactionResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final Clock clock;

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO, User user)
            throws NotFoundException {
        Category category = categoryService.findCategory(transactionRequestDTO.category().id(), user);
        Transaction transaction = TransactionMapper.fromDto(transactionRequestDTO, user, category);
        transaction = transactionRepository.save(transaction);

        if (transaction.getSubtype() == Subtype.FIXED) {
            backfillFixedTransactions(transaction, user, category);
        }

        return TransactionMapper.toDto(transaction);
    }

    private void backfillFixedTransactions(Transaction original, User user, Category category) {
        LocalDate today = LocalDate.now(clock);
        YearMonth originalMonth = YearMonth.from(original.getDateTransaction());
        YearMonth currentMonth = YearMonth.from(today);

        if (!originalMonth.isBefore(currentMonth)) {
            return;
        }

        YearMonth cursor = originalMonth.plusMonths(1);
        while (!cursor.isAfter(currentMonth)) {
            LocalDate recurringDate = sameDayForMonth(original.getDateTransaction(), cursor);
            boolean alreadyExists = transactionRepository.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                    user, category, original.getName(), original.getAmount(),
                    original.getType(), original.getSubtype(),
                    recurringDate.getYear(), recurringDate.getMonthValue());
            if (!alreadyExists) {
                transactionRepository.save(Transaction.builder()
                        .name(original.getName())
                        .dateTransaction(recurringDate)
                        .amount(original.getAmount())
                        .price(original.getPrice())
                        .type(original.getType())
                        .subtype(original.getSubtype())
                        .user(user)
                        .category(category)
                        .build());
            }
            cursor = cursor.plusMonths(1);
        }
    }

    @Override
    public Page<TransactionResponseDTO> getAllTransaction(
            User user,
            TransactionFilter filter,
            com.money.manager.domain.paging.Pageable pageable) {

        Sort sort = pageable.direction() == SortDirection.DESC
                ? Sort.by(pageable.sortBy()).descending()
                : Sort.by(pageable.sortBy()).ascending();

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(
                pageable.page(), pageable.size(), sort);

        org.springframework.data.domain.Page<Transaction> transactions = transactionRepository.findByFilters(
                user,
                filter.type(),
                filter.subtype(),
                filter.from(),
                filter.to(),
                springPageable);

        List<TransactionResponseDTO> content = transactions.getContent().stream().map(TransactionMapper::toDto).toList();

        return Page.of(content, transactions.getNumber(), transactions.getSize(),
                transactions.getTotalElements(), transactions.getTotalPages());
    }

    @Override
    public TransactionResponseDTO getTransaction(Long transactionId, User user) throws NotFoundException {
        Transaction transaction = findById(transactionId, user);
        return TransactionMapper.toDto(transaction);
    }

    @Override
    public TransactionResponseDTO updateTransaction(TransactionRequestDTO transactionRequestDTO, Long transactionId,User user) throws NotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Transaction transaction = findById(transactionId, user);
        transaction.setName(transactionRequestDTO.name());
        transaction.setDateTransaction( LocalDate.parse(transactionRequestDTO.transactionDate(), formatter));
        transaction.setPrice(transactionRequestDTO.price());
        transaction.setAmount(transactionRequestDTO.amount());
        transaction.setSubtype(Subtype.getSubTypeByName(transactionRequestDTO.transactionSubtype()));
        transaction.setType(Type.getTypeByName(transactionRequestDTO.transactionType()));
        transaction.setCategory(categoryService.findCategory(transactionRequestDTO.category().id(),
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

    static LocalDate sameDayForMonth(LocalDate original, YearMonth target) {
        int day = Math.min(original.getDayOfMonth(), target.lengthOfMonth());
        return LocalDate.of(target.getYear(), target.getMonth(), day);
    }
}
