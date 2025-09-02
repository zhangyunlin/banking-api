package com.tsb.banking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull Long customerId,
        @NotNull Long fromAccountId,
        @NotNull Long toAccountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        String memo,
        String idempotencyKey
) {
}
