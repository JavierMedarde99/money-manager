package com.money.manager.application.mappers;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.money.manager.domain.Category;
import com.money.manager.domain.Transaction;
import com.money.manager.domain.User;
import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;
import com.money.manager.infrastructure.dtos.TransactionRequestDTO;
import com.money.manager.infrastructure.dtos.TransactionResponseDTO;

public class TransactionMapper {
    public static Transaction fromDto(TransactionRequestDTO transactionRequestDTO, User user,Category category) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(transactionRequestDTO.transactionDate(), formatter);
        return Transaction.builder()
                .user(user)
                .category(category)
                .amount(transactionRequestDTO.amount())
                .name(transactionRequestDTO.name())
                .prices(transactionRequestDTO.price())
                .subtype(Subtype.getSubTypeByName(transactionRequestDTO.transactionSubtype()))
                .type(Type.getSubTypeByName(transactionRequestDTO.transactionType()))
                .dateTransaction(Date.valueOf(date))
                .build();
    }

    public static TransactionResponseDTO toDto(Transaction transaction){
        return new TransactionResponseDTO(transaction.getName(), transaction.getDateTransaction().toString(), 
        transaction.getAmount(), transaction.getPrices(), transaction.getType().getName(), 
        transaction.getSubtype().getName(), 
        CategoryMapper.toDto(transaction.getCategory()), transaction.getId());
    }
}
