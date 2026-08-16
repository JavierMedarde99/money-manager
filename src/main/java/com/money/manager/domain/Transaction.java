package com.money.manager.domain;

import java.time.LocalDate;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    private Long id;
    private String name;
    private LocalDate dateTransaction;
    private Integer amount;
    private Double prices;
    private Type type;
    private Subtype subtype;
    private User user;
    private Category category;
}
