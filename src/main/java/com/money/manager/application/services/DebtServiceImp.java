package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.money.manager.application.mappers.DebtMapper;
import com.money.manager.domain.Debt;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.services.DebtService;
import com.money.manager.infrastructure.dtos.DebtRequestDTO;
import com.money.manager.infrastructure.dtos.DebtResponseDTO;
import com.money.manager.infrastructure.persistance.PostgresDebtRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DebtServiceImp implements DebtService{

    private final PostgresDebtRepository debtRepository;

    @Override
    public DebtResponseDTO insertDebt(DebtRequestDTO debtRequestDTO, User user) {
        Debt debt = DebtMapper.fromDto(debtRequestDTO, user);
        debtRepository.save(debt);
        return DebtMapper.toDto(debt);
    }

    @Override
    public List<DebtResponseDTO> getDebts(User user) {
        List<Debt> listDebts = debtRepository.findByUser(user);
        return listDebts.stream().map(debt -> DebtMapper.toDto(debt)).toList();
    }

    @Override
    public DebtResponseDTO getDebt(Long id, User user) throws NotFoundException {
        Debt debt = getDebtById(id, user);
        return DebtMapper.toDto(debt);
    }

    @Override
    public DebtResponseDTO updateDebt(DebtRequestDTO debtRequestDTO, Long id, User user) throws NotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Debt debt = getDebtById(id, user);
        debt.setStartDate(LocalDate.parse(debtRequestDTO.starDate(), formatter));
        debt.setName(debtRequestDTO.name());
        debt.setTotalAmount(debtRequestDTO.totalAmount());
        debt.setEndDate(debtRequestDTO.endDate()== null ? null : LocalDate.parse(debtRequestDTO.endDate(), formatter));
        debtRepository.save(debt);
        return DebtMapper.toDto(debt);
    }

    @Override
    public String deleteDebt(Long id, User user) throws NotFoundException{
       Debt debt = getDebtById(id, user);
       debtRepository.delete(debt);
       return "debt delete";
    }

    private Debt getDebtById(Long id, User user) throws NotFoundException{
        Optional<Debt> optDebt = debtRepository.findByIdAndUser_Id(id, user.getId());
        return optDebt.orElseThrow(() -> new NotFoundException()); 
    }
    
}
