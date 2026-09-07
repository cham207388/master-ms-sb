package com.abcham.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "AmountRequest", description = "Request to deposit or withdraw funds")
public class AmountRequestDto {

    @NotNull(message = "Account number is required")
    @Schema(description = "Account number", example = "1234567890")
    private Long accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount in USD", example = "100.00")
    private BigDecimal amount;
}
