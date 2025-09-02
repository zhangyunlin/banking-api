package com.tsb.banking.api.dto;

import java.math.BigDecimal;

public record TransferResponseDto(
        Long debitTxnId,
        Long creditTxnId,
        BigDecimal fromBalance,
        BigDecimal toBalance
) {
}
