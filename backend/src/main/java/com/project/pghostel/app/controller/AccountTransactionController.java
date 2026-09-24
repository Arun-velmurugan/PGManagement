package com.project.pghostel.app.controller;

import com.project.pghostel.app.entity.AccountTransaction;
import com.project.pghostel.app.service.AccountTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin
public class AccountTransactionController {

    private final AccountTransactionService
            transactionService;

    public AccountTransactionController(
            AccountTransactionService transactionService) {

        this.transactionService = transactionService;
    }


    // Get all transactions

    @GetMapping
    public List<AccountTransaction>
    getAllTransactions() {

        return transactionService
                .getAllTransactions();
    }


    // Add expense

    @PostMapping("/expense")
    public ResponseEntity<?> addExpense(
            @RequestBody Map<String, Object> request) {

        try {

            String description =
                    request.get("description")
                            .toString();

            double amount =
                    Double.parseDouble(
                            request.get("amount")
                                    .toString());

            LocalDate date =
                    LocalDate.parse(
                            request.get("transactionDate")
                                    .toString());

            AccountTransaction transaction =
                    transactionService.addExpense(
                            description,
                            amount,
                            date
                    );

            return ResponseEntity.ok(transaction);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}