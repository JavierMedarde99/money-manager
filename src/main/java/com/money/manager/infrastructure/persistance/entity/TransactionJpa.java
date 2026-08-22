package com.money.manager.infrastructure.persistance.entity;

import java.time.LocalDate;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "transactions")
public class TransactionJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate dateTransaction;
    private Integer amount;
    @Column(name = "prices")
    private Double price;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Enumerated(EnumType.STRING)
    private Subtype subtype;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserJpa user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryJpa category;
}
