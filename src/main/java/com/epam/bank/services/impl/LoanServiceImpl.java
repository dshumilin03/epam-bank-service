package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UnknownStrategyTypeException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.LoanMapper;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.LoanService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BankAccountService bankAccountService;
    private final LoanMapper loanMapper;
    private final BankAccountMapper bankAccountMapper;

    @Override
    public List<LoanDto> getUserLoansByUserId(UUID id) {
        List<LoanDto> result = new ArrayList<>();
        List<Loan> loans = loanRepository.findByUserId(id);

        loans.forEach(loan -> result.add(loanMapper.toDto(loan)));
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
    public LoanDto open(LoanRequestDto loanRequestDto) {
        try {
            LoanDto loanDto = loanMapper.toDto(loanRequestDto);

            BankAccountDto bankAccount = bankAccountService.getById(loanRequestDto.bankAccountNumber());
            loanDto.setBankAccount(bankAccount);
            loanDto.setCreatedAt(LocalDateTime.now());
            loanDto.setLastChargeAt(loanDto.getCreatedAt());

            // monthly or daily
            if (loanRequestDto.chargeStrategyType().name().equals(ChargeStrategyType.MONTHLY.name())) {
                loanDto.setNextChargeAt(LocalDateTime.now().plusMonths(1));
            } else {
                loanDto.setNextChargeAt(LocalDateTime.now().plusDays(1));

            }

            Loan loan = loanMapper.toEntity(loanDto);
            LocalDateTime now = LocalDateTime.now();
            loan.setCreatedAt(now);
            loan.setBankAccount(bankAccountMapper.toEntity(loanDto.getBankAccount()));

            ChargeStrategyType strategyType = loan.getChargeStrategyType();

            if (strategyType == null) {
                throw new UnknownStrategyTypeException("Unknown strategy type");
            }

            switch (strategyType) {
                case DAILY -> loan.setNextChargeAt(now.plusDays(1));
                case MONTHLY -> loan.setNextChargeAt(now.plusMonths(1));
                default -> throw new UnknownStrategyTypeException("Unknown strategy type");
            }
            bankAccountService.deposit(bankAccount.bankAccountNumber(), loanRequestDto.moneyLeft());
            return loanMapper.toDto(loanRepository.save(loan));
        } catch (DataAccessException e) {
            log.error(e);
            throw e;
        }
    }

    @Override
    public LoanDto update(UUID transactionId, LoanDto loanDto) {

        Loan loan = loanRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Loan not found by Id"));

        Loan loanUpdate = loanMapper.toEntity(loanDto);

        updateFields(loan, loanUpdate);
        return loanMapper.toDto(loanRepository.save(loan));
    }

    @Override
    public LoanDto getById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Not found Loan by Id"));

        return loanMapper.toDto(loan);
    }

    @Override
    public Loan getEntityById(UUID loanId) {
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
