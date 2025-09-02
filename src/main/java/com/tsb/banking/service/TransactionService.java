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

  /**
   * Get paginated transactions for an account within an optional time range
   * @param accountId the account ID
   * @param from optional start time (inclusive), null means from the epoch
   * @param to optional end time (inclusive), null means now
   * @param page 0-based page index
   * @param size page size
   * @return
   */
  public Page<TransactionDto> transactionsForAccount(Long accountId, Instant from, Instant to, int page, int size) {
    Instant f = (from == null) ? Instant.EPOCH : from;
    Instant t = (to == null) ? Instant.now() : to;
    return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        accountId, f, t, PageRequest.of(page, size))
      .map(tx -> new TransactionDto(tx.getId(), tx.getType(), tx.getAmount(), tx.getCurrency(),
                            tx.getReference(), tx.getCounterparty(), tx.getCreatedAt()));
  }
}
