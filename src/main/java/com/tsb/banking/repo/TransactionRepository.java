package com.tsb.banking.repo;

import com.tsb.banking.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  Page<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
      Long accountId, Instant from, Instant to, Pageable pageable);
}
