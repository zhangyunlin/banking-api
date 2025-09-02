package com.tsb.banking.api;

import com.tsb.banking.api.dto.AccountDto;
import com.tsb.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author zhangyunlin
 *
 * Show all accounts for a customer
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

  private final AccountService service;
  public CustomerController(AccountService service) { this.service = service; }

  @Operation(summary = "Show all accounts for a customer")
  @GetMapping("/{customerId}/accounts")
  public List<AccountDto> accounts(@PathVariable Long customerId) {
    return service.accountsForCustomer(customerId);
  }
}
