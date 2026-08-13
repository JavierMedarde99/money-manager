package com.money.manager.application.ports;

import java.util.List;

import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;

public interface DebtService {
    DebtResponseDTO insertDebt(DebtRequestDTO debtRequestDTO,User user);
    List<DebtResponseDTO> getDebts(User user);
    DebtResponseDTO getDebt(Long id) throws NotFoundException;
    DebtResponseDTO updateDebt(DebtRequestDTO debtRequestDTO,Long id, User user) throws NotFoundException;
    String deleteDebt(Long id) throws NotFoundException;
}
