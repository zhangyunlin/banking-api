package com.tsb.banking.api;

import com.tsb.banking.api.dto.TransactionDto;
import com.tsb.banking.service.AccountService;
import com.tsb.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * @author zhangyunlin
 *
 * List transactions for an account, with optional from/to date range and paging
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final AccountService accountService;
  private final TransactionService txnService;

  public AccountController(AccountService accountService, TransactionService txnService) {
    this.accountService = accountService;
    this.txnService = txnService;
  }

  @Operation(summary = "Show all transactions for an account (optional from/to, paging)")
  @GetMapping("/{accountId}/transactions")
  public Page<TransactionDto> transactions(
      @PathVariable Long accountId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    // ensure no 404 returned if account not found
    accountService.requireAccount(accountId);
    return txnService.transactionsForAccount(accountId, from, to, page, size);
  }
}
