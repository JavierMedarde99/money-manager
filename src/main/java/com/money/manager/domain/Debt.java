package com.money.manager.domain;

import java.time.LocalDate;
import java.util.Set;

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
public class Debt {
    private Long id;
    private String name;
    private Double totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private User user;
    private Set<Payment> payments;

    public void endDebt(){
        this.endDate = LocalDate.now();
    }
}
