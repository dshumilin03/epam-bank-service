package com.epam.bank.domain.impl;

import com.epam.bank.domain.BankAccountProvider;
import com.epam.bank.domain.LoanFactory;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Loan;
import com.epam.bank.services.strategies.ChargeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoanFactoryImpl implements LoanFactory {
    // todo add to logging aop all package
    private final BankAccountProvider bankAccountProvider;

    @Override
    public Loan createLoan(LoanRequestDto loanRequestDto) {
        LocalDateTime now = LocalDateTime.now();

        ChargeStrategy strategy = ChargeStrategy.getChargeStrategy(loanRequestDto.chargeStrategyType());
        LocalDateTime nextChargeAt = strategy.calculateNextChargeDate(now);

        BankAccount bankAccount = bankAccountProvider.getOrThrow(loanRequestDto.bankAccountNumber());
        return Loan.builder().
                moneyLeft(loanRequestDto.moneyLeft())
                .percent(loanRequestDto.percent())
                .chargeStrategyType(loanRequestDto.chargeStrategyType())
                .bankAccount(bankAccount)
                .createdAt(LocalDateTime.now())
                .lastChargeAt(now)
                .nextChargeAt(nextChargeAt)
                .termMonths(loanRequestDto.termMonths())
                .build();
    }
}
