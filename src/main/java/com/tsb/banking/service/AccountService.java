package com.tsb.banking.service;

import com.tsb.banking.api.dto.AccountDto;
import com.tsb.banking.exception.NotFoundException;
import com.tsb.banking.model.Account;
import com.tsb.banking.repo.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public List<AccountDto> accountsForCustomer(Long customerId) {
    return accountRepository.findByCustomerId(customerId).stream()
        .map(a -> new AccountDto(a.getId(), a.getNumber(), a.getCurrency(), a.getBalance()))
        .toList();
  }

  // throw NotFoundException if not found
  public Account requireAccount(Long id) {
    return accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account " + id + " not found"));
  }
}
