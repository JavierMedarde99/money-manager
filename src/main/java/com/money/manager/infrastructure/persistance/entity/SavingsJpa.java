package com.money.manager.infrastructure.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Entity
@Table(name = "savings", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "year", "month" }))
public class SavingsJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int month;

    @Column(name = "total_income")
    private double totalIncome;

    @Column(name = "total_expense")
    private double totalExpense;

    private double savings;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserJpa user;
}
