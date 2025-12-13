package com.epam.bank.services.impl;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.CardDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.*;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.CardMapper;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;
    private final TransactionRepository transactionRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public BankAccountDTO create(UUID userId, CardDTO cardDTO) {
        bankAccountRepository.findByCardId(cardDTO.getId()).orElseThrow(() -> new ExistsException("Card is attached to bank account"));
        BankAccount newBankAccount = new BankAccount();

        newBankAccount.setUser(userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found by Id")));
        AbstractCard abstractCard = cardMapper.toEntity(cardDTO);
        newBankAccount.setCard(abstractCard);
        newBankAccount.setIncomingTransactions(new ArrayList<>());
        newBankAccount.setOutgoingTransactions(new ArrayList<>());

        switch (abstractCard.getType()) {
            case CardType.DEBIT -> {
                newBankAccount.setMoneyAmount(BigDecimal.valueOf(0));
                newBankAccount.setMoneyAmount(BigDecimal.valueOf(0));
            }
            case CardType.CREDIT -> {
                CreditCard creditCard = (CreditCard) abstractCard;
                newBankAccount.setMoneyAmount(creditCard.getCreditLimit());
            }
            default -> throw new IllegalArgumentException("Unknown card type");
        }

        return bankAccountMapper.toDTO(bankAccountRepository.save(newBankAccount));
    }

    @Override
    @Transactional
    public TransactionStatus processTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found by Id"));
        BankAccount source = transaction.getSource();
        BankAccount target = transaction.getTarget();
        if (target == null) {

        }
// todo: нет проверки на недостаток денег
        try {
            source.setMoneyAmount(source.getMoneyAmount().subtract(transaction.getMoneyAmount()));
            target.setMoneyAmount(target.getMoneyAmount().add(transaction.getMoneyAmount()));
            bankAccountRepository.save(source);
            bankAccountRepository.save(target);
            transaction.setStatus(TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
        } finally {
            transactionRepository.save(transaction);
        }

        return transaction.getStatus();
    }

    @Override
    public BankAccountDTO getById(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found bank account by id (number of account)"));

        return bankAccountMapper.toDTO(bankAccount);
    }

    @Override
    public List<TransactionDTO> getTransactions(Long id, boolean outgoing) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found bank account by id (number of account)"));

        List<Transaction> transactions = outgoing ? bankAccount.getOutgoingTransactions() : bankAccount.getIncomingTransactions();
        List<TransactionDTO> transactionDTOS = new ArrayList<>();

        transactions.forEach(transaction -> {
            transactionDTOS.add(transactionMapper.toTransactionDTO(transaction));
        });

        return transactionDTOS;
    }
}
