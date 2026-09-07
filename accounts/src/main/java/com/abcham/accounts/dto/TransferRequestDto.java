package com.abcham.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "TransferRequest", description = "Request to transfer funds between accounts")
public class TransferRequestDto {

    @NotNull(message = "Source account number is required")
    @Schema(description = "Source account number", example = "1234567890")
    private Long sourceAccountNumber;

    @NotNull(message = "Destination account number is required")
    @Schema(description = "Destination account number", example = "9876543210")
    private Long destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount in USD", example = "50.00")
    private BigDecimal amount;
}
