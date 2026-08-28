package com.money.manager.infrastructure.persistance.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.domain.Transaction;
import com.money.manager.domain.TransactionRepository;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.infrastructure.persistance.PostgresCategoryRepository;
import com.money.manager.infrastructure.persistance.PostgresTransactionRepository;
import com.money.manager.infrastructure.persistance.PostgresUserRepository;
import com.money.manager.infrastructure.persistance.entity.CategoryJpa;
import com.money.manager.infrastructure.persistance.entity.UserJpa;
import com.money.manager.infrastructure.persistance.mapper.TransactionJpaMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final PostgresTransactionRepository jpa;
    private final PostgresUserRepository jpaUser;
    private final PostgresCategoryRepository jpaCategory;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findByUser(com.money.manager.domain.User user) {
        return jpa.findByUser_Id(user.getId()).stream().map(TransactionJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findBySubtype(Subtype subtype) {
        return jpa.findBySubtype(subtype).stream().map(TransactionJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserCategoryNameAmountTypeSubtypeAndMonth(
            com.money.manager.domain.User user,
            com.money.manager.domain.Category category,
            String name, Integer amount, Type type, Subtype subtype, int year, int month) {
        UserJpa userJpa = jpaUser.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        CategoryJpa categoryJpa = jpaCategory.findById(category.getId())
                .orElseThrow(() -> new IllegalStateException("category not found"));
        return jpa.existsByUserCategoryNameAmountTypeSubtypeAndMonth(
                userJpa, categoryJpa, name, amount, type, subtype, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findByUserAndCategory(com.money.manager.domain.User user,
            com.money.manager.domain.Category category) {
        return jpa.findByUser_IdAndCategory_Id(user.getId(), category.getId()).stream()
                .map(TransactionJpaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(Long id) {
        return jpa.findById(id).map(TransactionJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findByIdAndUser_Id(Long id, Long userId) {
        return jpa.findByIdAndUser_Id(id, userId).map(TransactionJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        UserJpa userJpa = jpaUser.findById(transaction.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        CategoryJpa categoryJpa = jpaCategory.findById(transaction.getCategory().getId())
                .orElseThrow(() -> new IllegalStateException("category not found"));
        return TransactionJpaMapper.toDomain(jpa.save(TransactionJpaMapper.toJpa(transaction, userJpa, categoryJpa)));
    }

    @Override
    @Transactional
    public void delete(Transaction transaction) {
        jpa.deleteById(transaction.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findByFilters(com.money.manager.domain.User user, Type type, Subtype subtype,
            LocalDate from, LocalDate to, Pageable pageable) {
        UserJpa userJpa = jpaUser.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("user not found"));
        return jpa.findByFilters(userJpa, type, subtype, from, to, pageable)
                .map(TransactionJpaMapper::toDomain);
    }
}
