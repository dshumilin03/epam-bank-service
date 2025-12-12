package com.epam.bank.services.impl;

import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.LoanMapper;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.services.LoanService;
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
    private final LoanMapper loanMapper;

    @Override
    public List<LoanDTO> getUserLoansByUserId(UUID id) {
        List<LoanDTO> result = new ArrayList<>();
        List<Loan> loans = loanRepository.findByUserId(id);

        loans.forEach(loan -> {
            result.add(loanMapper.toLoanDto(loan));
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
    public LoanDTO open(LoanDTO loanDTO) {
        Loan loan = loanMapper.toLoan(loanDTO);
        LocalDateTime now = LocalDateTime.now();
        loan.setCreatedAt(now);
        ChargeStrategyType strategyType = loan.getChargeStrategy();

        switch (strategyType) {
            case DAILY -> loan.setNextChargeAt(now.plusDays(1));
            case MONTHLY -> loan.setNextChargeAt(now.plusMonths(1));
            default -> throw new IllegalArgumentException("Unknown strategy type");
        }
        return loanMapper.toLoanDto(loanRepository.save(loan));
    }

    @Override
    public LoanDTO update(LoanDTO loanDTO) {
        Loan loan = loanMapper.toLoan(loanDTO);

        return loanMapper.toLoanDto(loanRepository.save(loan));
    }
}
