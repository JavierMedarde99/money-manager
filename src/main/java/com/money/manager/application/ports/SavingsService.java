package com.money.manager.application.ports;

import java.util.List;

import com.money.manager.application.dtos.SavingsResponseDTO;
import com.money.manager.domain.User;

public interface SavingsService {

    void recalculate(User user, int year, int month);

    void recalculateAll();

    List<SavingsResponseDTO> getSavings(User user);
}
