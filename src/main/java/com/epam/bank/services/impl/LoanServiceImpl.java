package com.epam.bank.services.impl;

import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UnknownStrategyTypeException;
import com.epam.bank.mappers.LoanMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.services.BankAccountService;
import com.epam.bank.services.LoanService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BankAccountService bankAccountService;
    private final LoanMapper loanMapper;
    private final BankAccountRepository bankAccountRepository;

    private static final String NOT_FOUND_BY_ID = "Loan not found by Id";

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getUserLoansByUserId(UUID id) {
        return loanRepository.findByUserId(id)
                .stream()
                .map(loanMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void close(UUID id) {
        // todo logic of closing may be added, may be loan status
        loanRepository.delete(getOrThrowById(id));
    }

    @Override
    @Transactional
    public LoanDto open(LoanRequestDto loanRequestDto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextChargeAt;

        switch (loanRequestDto.chargeStrategyType()) {
            case ChargeStrategyType.MONTHLY -> nextChargeAt = now.plusMonths(1);
            case ChargeStrategyType.DAILY -> nextChargeAt = now.plusDays(1);
            default -> throw new UnknownStrategyTypeException("Got unknown strategy from request");
        }

        // todo create loan factory
        BankAccount bankAccount = bankAccountRepository.findById(loanRequestDto.bankAccountNumber())
                .orElseThrow(() -> new NotFoundException("Not found bank account by number"));
        Loan loan = Loan.builder().
                moneyLeft(loanRequestDto.moneyLeft())
                .percent(loanRequestDto.percent())
                .chargeStrategyType(loanRequestDto.chargeStrategyType())
                .bankAccount(bankAccount)
                .createdAt(LocalDateTime.now())
                .nextChargeAt(nextChargeAt)
                .lastChargeAt(now)
                .termMonths(loanRequestDto.termMonths())
                .build();

        //todo need event when loan created deposit money
        bankAccountService.deposit(bankAccount.getBankAccountNumber(), loanRequestDto.moneyLeft());
        return loanMapper.toDto(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public LoanDto update(UUID id, LoanDto loanDto) {

        Loan loan = getOrThrowById(id);
        Loan loanUpdate = loanMapper.toEntity(loanDto);

        updateFields(loan, loanUpdate);
        return loanMapper.toDto(loanRepository.save(loan));
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDto getById(UUID loanId) {
        return loanMapper.toDto(getOrThrowById(loanId));
    }

    @Override
    @Transactional(readOnly = true)
    public Loan getEntityById(UUID loanId) {
        return getOrThrowById(loanId);
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

    private Loan getOrThrowById(UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BY_ID));
    }
}
