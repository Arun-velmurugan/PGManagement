package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTransactionRepository
        extends JpaRepository<AccountTransaction, Long> {

    Optional<AccountTransaction>
    findByTransactionId(String transactionId);
}