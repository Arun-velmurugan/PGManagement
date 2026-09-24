package com.project.pghostel.app.service;

import com.project.pghostel.app.entity.AccountTransaction;
import com.project.pghostel.app.repository.AccountTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountTransactionService {

    private final AccountTransactionRepository
            transactionRepository;

    public AccountTransactionService(
            AccountTransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }


    // Get all transactions
    public List<AccountTransaction> getAllTransactions() {

        return transactionRepository.findAll();
    }


    // Add expense
    public AccountTransaction addExpense(
            String description,
            double amount,
            LocalDate transactionDate) {

        AccountTransaction transaction =
                new AccountTransaction();

        transaction.setTransactionId(
                generateExpenseId()
        );

        transaction.setDescription(description);

        transaction.setAmount(amount);

        transaction.setTransactionDate(
                transactionDate
        );

        transaction.setStatus("EXPENSE");

        return transactionRepository.save(transaction);
    }


    // Add income from paid payment
    public AccountTransaction addIncome(
            String description,
            double amount,
            LocalDate transactionDate,
            Long paymentId) {

        AccountTransaction transaction =
                new AccountTransaction();

        transaction.setTransactionId(
                "INT" + String.format(
                        "%04d", paymentId)
        );

        transaction.setDescription(description);

        transaction.setAmount(amount);

        transaction.setTransactionDate(
                transactionDate
        );

        transaction.setStatus("INCOME");

        return transactionRepository.save(transaction);
    }


    // Generate expense ID
    private String generateExpenseId() {

        long count =
                transactionRepository
                        .findAll()
                        .stream()
                        .filter(t ->
                                "EXPENSE".equalsIgnoreCase(
                                        t.getStatus()))
                        .count();

        return "EXT" + String.format(
                "%04d", count + 1);
    }
}