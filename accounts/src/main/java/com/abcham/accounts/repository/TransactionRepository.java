package com.abcham.accounts.repository;

import com.abcham.accounts.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(Long accountNumber);

    void deleteByAccountNumber(Long accountNumber);
}
