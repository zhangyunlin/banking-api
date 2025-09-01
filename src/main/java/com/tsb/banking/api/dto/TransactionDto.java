package com.tsb.banking.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(Long id,
                             String type,
                             BigDecimal amount,
                             String currency,
                             String reference,
                             String counterparty,
                             Instant createdAt) {
}
