package com.tsb.banking.api.dto;

import java.math.BigDecimal;

public record AccountDto(Long id,
                         String number,
                         String currency,
                         BigDecimal balance) {
}
