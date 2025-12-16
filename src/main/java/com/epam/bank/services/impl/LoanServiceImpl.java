package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.LoanMapper;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.Chargeable;
import com.epam.bank.services.LoanService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BankAccountService bankAccountService;
    private final LoanMapper loanMapper;
    private final BankAccountMapper bankAccountMapper;

    @Override
    public List<LoanDTO> getUserLoansByUserId(UUID id) {
        List<LoanDTO> result = new ArrayList<>();
        List<Loan> loans = loanRepository.findByUserId(id);

        loans.forEach(loan -> {
            result.add(loanMapper.toDTO(loan));
        });
        return result;
    }

    @Override
    public void close(UUID id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found by Id"));

        loanRepository.delete(loan);
    }

    @Override
    @Transactional
    public LoanDTO open(LoanRequestDTO loanRequestDTO) {
        LoanDTO loanDTO = loanMapper.toDTO(loanRequestDTO);

        BankAccountDTO bankAccount = bankAccountService.getById(loanRequestDTO.bankAccountNumber());
        loanDTO.setBankAccount(bankAccount);
        loanDTO.setCreatedAt(LocalDateTime.now());

        // monthly or daily
        if (loanRequestDTO.chargeStrategyType().name().equals(ChargeStrategyType.MONTHLY.name())) {
            loanDTO.setNextChargeAt(LocalDateTime.now().plusMonths(1));
        } else {
            loanDTO.setNextChargeAt(LocalDateTime.now().plusDays(1));

        }

        Loan loan = loanMapper.toEntity(loanDTO);
        LocalDateTime now = LocalDateTime.now();
        loan.setCreatedAt(now);
        loan.setBankAccount(bankAccountMapper.toEntity(loanDTO.getBankAccount()));

        ChargeStrategyType strategyType = loan.getChargeStrategyType();

        if (strategyType == null) {
            throw new IllegalArgumentException("Unknown strategy type");
        }

        switch (strategyType) {
            case DAILY -> loan.setNextChargeAt(now.plusDays(1));
            case MONTHLY -> loan.setNextChargeAt(now.plusMonths(1));
            default -> throw new IllegalArgumentException("Unknown strategy type");
        }
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Override
    public LoanDTO update(UUID transactionId, LoanDTO loanDTO) {

        Loan loan = loanRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Loan not found by Id"));

        Loan loanUpdate = loanMapper.toEntity(loanDTO);

        updateFields(loan, loanUpdate);
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Override
    public LoanDTO getById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Not found Loan by Id"));

        return loanMapper.toDTO(loan);
    }

    @Override
    public Chargeable getEntityById(UUID loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Not found Loan by Id"));
    }

    private void updateFields(Loan loan, Loan update) {

        loan.setMoneyLeft(update.getMoneyLeft());
        loan.setCreatedAt(update.getCreatedAt());
        loan.setPercent(update.getPercent());
        loan.setTermMonths(update.getTermMonths());
        loan.setChargeStrategyType(update.getChargeStrategyType());
        loan.setBankAccount(update.getBankAccount());
        loan.setNextChargeAt(update.getNextChargeAt());
        loan.setLastChargeAt(update.getLastChargeAt());
    }

}
