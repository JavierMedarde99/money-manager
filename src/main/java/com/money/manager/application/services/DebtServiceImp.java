package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.application.mappers.DebtMapper;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.application.ports.DebtService;
import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DebtServiceImp implements DebtService{

    private final DebtRepository debtRepository;

    @Override
    public DebtResponseDTO insertDebt(DebtRequestDTO debtRequestDTO, User user) {
        Debt debt = DebtMapper.fromDto(debtRequestDTO, user);
        debt = debtRepository.save(debt);
        return DebtMapper.toDto(debt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtResponseDTO> getDebts(User user) {
        List<Debt> listDebts = debtRepository.findByUser(user);
        return listDebts.stream().map(debt -> DebtMapper.toDto(debt)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DebtResponseDTO getDebt(Long id, User user) throws NotFoundException {
        Debt debt = getDebtById(id, user);
        return DebtMapper.toDto(debt);
    }

    @Override
    public DebtResponseDTO updateDebt(DebtRequestDTO debtRequestDTO, Long id, User user) throws NotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Debt debt = getDebtById(id, user);
        debt.setStartDate(LocalDate.parse(debtRequestDTO.startDate(), formatter));
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
