package com.epam.bank.services.impl;

import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.ChargeService;
import com.epam.bank.services.Chargeable;
import com.epam.bank.services.strategies.ChargeStrategy;
import com.epam.bank.services.strategies.DailyChargeStrategy;
import com.epam.bank.services.strategies.MonthlyChargeStrategy;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.bank.entities.ChargeStrategyType.DAILY;
import static com.epam.bank.entities.ChargeStrategyType.MONTHLY;

@AllArgsConstructor
@Service
@Log4j2
public class ChargeServiceImpl implements ChargeService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private final Map<ChargeStrategyType, ChargeStrategy> strategies = Map.of(
            DAILY, new DailyChargeStrategy(),
            MONTHLY, new MonthlyChargeStrategy());

    @Transactional
    public void applyCharge(Chargeable chargeable) {

        try {
            ChargeStrategy strategy = strategies.get(chargeable.getChargeStrategyType());

            BigDecimal chargeAmount = strategy.calculateCharge(chargeable.getDebt(), chargeable.getPercent());
            chargeable.setLastChargeAt(LocalDateTime.now());
            chargeable.setNextChargeAt(calculateNextChargeDate(chargeable.getChargeStrategyType()));
            Transaction newTransaction = Transaction.builder()
                    .source(chargeable.getBankAccount())
                    .createdAt(chargeable.getLastChargeAt())
                    .description("This is charge with ID: " + chargeable.getId())
                    .transactionType(TransactionType.CHARGE)
                    .status(TransactionStatus.PENDING)
                    .moneyAmount(chargeAmount)
                    .build();

            transactionMapper.toDTO(transactionRepository.save(newTransaction));
        } catch (DataAccessException ex) {
            log.error("Database connection error: {}", ex.getMessage());
            throw ex;
        }

    }

    private LocalDateTime calculateNextChargeDate(ChargeStrategyType chargeStrategy) {
        return switch (chargeStrategy) {
            case DAILY -> LocalDateTime.now().plusDays(1);
            case MONTHLY -> LocalDateTime.now().plusMonths(1);
        };
    }
}
