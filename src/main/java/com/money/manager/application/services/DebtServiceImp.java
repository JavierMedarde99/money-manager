package com.money.manager.application.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.money.manager.application.mappers.DebtMapper;
import com.money.manager.application.mappers.PaymentMapper;
import com.money.manager.domain.Debt;
import com.money.manager.domain.DebtRepository;
import com.money.manager.domain.Payment;
import com.money.manager.domain.PaymentRepository;
import com.money.manager.domain.User;
import com.money.manager.domain.exception.NotFoundException;
import com.money.manager.domain.paging.Page;
import com.money.manager.domain.paging.Pageable;
import com.money.manager.domain.paging.SortDirection;
import com.money.manager.application.ports.DebtService;
import com.money.manager.application.dtos.DebtRequestDTO;
import com.money.manager.application.dtos.DebtResponseDTO;
import com.money.manager.application.dtos.PaymentResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DebtServiceImp implements DebtService{

    private final DebtRepository debtRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public DebtResponseDTO insertDebt(DebtRequestDTO debtRequestDTO, User user) {
        Debt debt = DebtMapper.fromDto(debtRequestDTO, user);
        debt = debtRepository.save(debt);
        return DebtMapper.toDto(debt, emptyPaymentsPage());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtResponseDTO> getDebts(User user, Pageable pageable) {
        Sort sort = pageable.direction() == SortDirection.DESC
                ? Sort.by(pageable.sortBy()).descending()
                : Sort.by(pageable.sortBy()).ascending();

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(
                pageable.page(), pageable.size(), sort);

        List<Debt> listDebts = debtRepository.findByUser(user);
        return listDebts.stream()
                .map(debt -> DebtMapper.toDto(debt, toPaymentsPage(
                        paymentRepository.findByDebt_Id(debt.getId(), springPageable))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DebtResponseDTO getDebt(Long id, User user) throws NotFoundException {
        Debt debt = getDebtById(id, user);
        return DebtMapper.toDto(debt, toPaymentsPage(debt.getPayments()));
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
        return DebtMapper.toDto(debt, toPaymentsPage(debt.getPayments()));
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

    @Override
    @Transactional
    public void closeDebt(Debt debt) {
        debt.endDebt();
        debtRepository.save(debt);
    }

    private Page<PaymentResponseDTO> toPaymentsPage(org.springframework.data.domain.Page<Payment> springPage) {
        List<PaymentResponseDTO> content = springPage.getContent().stream()
                .map(PaymentMapper::toDto).toList();
        return Page.of(content, springPage.getNumber(), springPage.getSize(),
                springPage.getTotalElements(), springPage.getTotalPages());
    }

    private Page<PaymentResponseDTO> toPaymentsPage(java.util.Set<Payment> payments) {
        List<Payment> list = payments == null ? List.of() : payments.stream().toList();
        List<PaymentResponseDTO> content = list.stream().map(PaymentMapper::toDto).toList();
        return Page.of(content, 0, content.size(), content.size(), content.isEmpty() ? 0 : 1);
    }

    private Page<PaymentResponseDTO> emptyPaymentsPage() {
        return Page.of(List.of(), 0, 10, 0, 0);
    }
    
}
