package com.money.manager.application.dtos;

import java.time.LocalDate;

import com.money.manager.domain.enums.Subtype;
import com.money.manager.domain.enums.Type;

public record TransactionFilter(Type type,
        Subtype subtype,
        LocalDate from,
        LocalDate to) {

}
