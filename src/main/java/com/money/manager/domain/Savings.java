package com.money.manager.domain;

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
public class Savings {
    private Long id;
    private User user;
    private int year;
    private int month;
    private double totalIncome;
    private double totalExpense;
    private double savings;
}
