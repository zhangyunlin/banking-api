package com.tsb.banking.service;

import com.tsb.banking.api.dto.TransactionDto;
import com.tsb.banking.repo.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Page<TransactionDto> transactionsForAccount(Long accountId, Instant from, Instant to, int page, int size) {
    Instant f = (from == null) ? Instant.EPOCH : from;
    Instant t = (to == null) ? Instant.now() : to;
    return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        accountId, f, t, PageRequest.of(page, size))
      .map(tx -> new TransactionDto(tx.getId(), tx.getType(), tx.getAmount(), tx.getCurrency(),
                            tx.getReference(), tx.getCounterparty(), tx.getCreatedAt()));
  }
}
