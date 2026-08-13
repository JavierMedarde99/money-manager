package com.money.manager.domain;

import java.util.Optional;

public interface PaymentReposiory {
    Optional<Payment> findByIdAndDebt_User_Id(Long id, Long userId);
}
