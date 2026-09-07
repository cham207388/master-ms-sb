package com.abcham.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(name = "Transaction", description = "Account transaction history entry")
public class TransactionDto {

    private Long id;
    private Long accountNumber;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long counterpartyAccount;
    private String description;
    private LocalDateTime createdAt;
}
